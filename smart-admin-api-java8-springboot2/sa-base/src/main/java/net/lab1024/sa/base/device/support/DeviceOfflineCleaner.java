package net.lab1024.sa.base.device.support;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.cluster.ClusterManager;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * 设备离线清理器 — 发送链路/详情等入口发现会话缺失（kill -9/断电等异常场景下设备必然离线，
 * 但 Redis/DB 残留"在线"）时，清除设备存储（Redis Hash）的上线字段并持久化离线状态（数据库）。
 * 提供 checkAndClean 在线校验（接触即校验）：在线返回 true，离线则清理残留并返回 false。
 * <p>
 * 幂等：清理动作重复执行无副作用（removeConfigs 不存在字段无操作；DB 更新带状态护栏）。
 * 清理失败只记录日志，不阻断调用方原有的"设备离线"报错。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/27
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
public class DeviceOfflineCleaner {

    /** 离线清理字段列表 — 与 DefaultDecodedClientMessageHandler.handleUnregister 同口径（同源引用，防两处列表漂移） */
    public static final String[] OFFLINE_CLEAR_FIELDS = {
            DeviceField.SESSION_ID.getValue(),
            DeviceField.GATEWAY_ID.getValue(),
            DeviceField.PARENT_DEVICE_ID.getValue(),
            DeviceField.PROTOCOL_ID.getValue(),
            DeviceField.CONNECTION_SERVER_ID.getValue()
    };

    private final DeviceSessionManager sessionManager;
    private final DeviceRegistry registry;
    /** 可空 — 单机（iot.cluster.enabled=false）不注入，在线校验跳过集群节点归属判定 */
    private final ClusterManager clusterManager;
    /** 可空 — 业务模块（sa-admin）未实现注入时仅清 Redis，不落库 */
    private final DeviceOnlineStatePersistence onlineStatePersistence;

    public DeviceOfflineCleaner(DeviceSessionManager sessionManager,
                                DeviceRegistry registry,
                                ClusterManager clusterManager,
                                DeviceOnlineStatePersistence onlineStatePersistence) {
        this.sessionManager = sessionManager;
        this.registry = registry;
        this.clusterManager = clusterManager;
        this.onlineStatePersistence = onlineStatePersistence;
    }

    /**
     * 离线清理 — 会话缺失即设备离线（Redis/DB 残留未更新），清除上线字段 + 持久化离线状态。
     * 竞态防护：清理前重查本地存活会话 — 设备毫秒级重连（会话复活）则放弃清理，避免误清。
     * 注意：不能用 switchIfEmpty 做"存活则跳过"判定 — Mono<Void> 无值可发、永远只可能 empty，
     * switchIfEmpty 必然触发兜底；用 hasElement 显式判定存活与否。
     */
    public Mono<Void> clean(String deviceId) {
        return sessionManager.getSession(deviceId, true)        // 死会话自动移除并走标准 unregister 清理链
                .filter(DeviceSession::isAlive)                 // 仅保留存活会话
                .hasElement()                                   // 存活 → true；离线/无会话 → false（恒发值，不会 empty）
                .flatMap(alive -> alive ? Mono.<Void>empty() : doClean(deviceId));
    }

    /**
     * 在线校验（接触即校验）— 返回设备是否在线：
     * ① 本地存活会话 → 在线（内存最权威，不做任何写操作）
     * ② 否则读 Redis 节点归属（集群跨节点场景）：归属节点存活 → 在线（设备在其他节点，不清理）
     * ③ 无归属 / 归属节点已死（kill -9/断电等异常残留）→ 离线 → 清除残留（Redis/DB）后返回 false
     */
    public Mono<Boolean> checkAndClean(String deviceId) {
        return sessionManager.getSession(deviceId, true)        // 死会话自动移除并走标准 unregister 清理链
                .filter(DeviceSession::isAlive)
                .map(session -> Boolean.TRUE)
                .switchIfEmpty(registry.getDevice(deviceId)
                        .flatMap(DeviceOperator::getConnectionServerId)
                        .filter(StringUtils::isNotEmpty)
                        .flatMap(nodeId -> {
                            // 集群：设备归属节点存活 → 在线（不清理）
                            if (clusterManager != null && clusterManager.getServerNode(nodeId) != null) {
                                return Mono.just(Boolean.TRUE);
                            }
                            return Mono.<Boolean>empty();
                        })
                        .switchIfEmpty(clean(deviceId).thenReturn(Boolean.FALSE)));
    }

    /**
     * 直接清理（不做会话重查）— 仅限 unregister 事件链调用（DefaultDecodedClientMessageHandler.handleUnregister）：
     * 事件触发时会话已从管理器移除、设备必然离线，重查 100% 多余；
     * 且事件链的移除路径带 predicate/原子性保护（见 MqttConnectionSession），不存在"新会话存活却被误清"的竞态。
     */
    public Mono<Void> cleanDirect(String deviceId) {
        return doClean(deviceId);
    }

    private Mono<Void> doClean(String deviceId) {
        // 不校验存在性：产品被禁用/删除时设备 exist()=false（getDevice 返回 empty），但 Redis 上线字段残留仍需清理（removeConfigs 幂等）
        return registry.getDeviceIgnoreExist(deviceId)
                .flatMap(operator -> {
                    operator.removeConfigs(OFFLINE_CLEAR_FIELDS);
                    operator.setConfigs(Collections.singletonMap(
                            DeviceField.OFFLINE_TIME.getValue(), System.currentTimeMillis()));
                    if (onlineStatePersistence != null) {
                        onlineStatePersistence.onOffline(deviceId);
                    }
                    return Mono.<Void>empty();
                })
                // 清理失败不阻断调用方"设备离线"报错 — 残留自愈于设备下次上下线事件
                .onErrorResume(err -> {
                    log.warn("[设备离线清理] 失败 deviceId={}", deviceId, err);
                    return Mono.empty();
                });
    }
}
