package net.lab1024.sa.base.device.support;

import net.lab1024.sa.base.cluster.ClusterManager;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.message.AbstractDeviceMessage;
import net.lab1024.sa.base.common.message.DeviceMessage;
import net.lab1024.sa.base.common.message.DeviceMessageReply;
import net.lab1024.sa.base.device.DeviceMessageSendService;
import net.lab1024.sa.base.device.DeviceMessageSender;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

/**
 * 集群路由发送器 — 设备可能不在本节点：
 * ① Redis CONNECTION_SERVER_ID 取设备所在节点
 * ② 本地节点 → LocalDeviceMessageSender 直接发送
 * ③ 远程节点 → clusterManager.getService(nodeId, DeviceMessageSendService) 远程代理，经 Hazelcast 转发到目标节点执行
 * 构造时注册本节点的 DeviceMessageSendService（其他节点经集群代理调用本节点设备发送）。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public class ClusterDeviceMessageSender implements DeviceMessageSender {

    private final ClusterManager clusterManager;
    private final LocalDeviceMessageSender localSender;
    private final DeviceRegistry registry;
    private final DeviceSessionManager sessionManager;

    public ClusterDeviceMessageSender(ClusterManager clusterManager,
                                      LocalDeviceMessageSender localSender,
                                      DeviceRegistry registry,
                                      DeviceSessionManager sessionManager) {
        this.clusterManager = clusterManager;
        this.localSender = localSender;
        this.registry = registry;
        this.sessionManager = sessionManager;
        // 注册本节点远程发送服务 — 其他节点 getService(nodeId, DeviceMessageSendService) 可调用本节点设备
        clusterManager.register(DeviceMessageSendService.class, new DefaultDeviceMessageSendService(localSender));
    }

    @Override
    public Mono<DeviceMessageReply> sendAndWait(DeviceMessage message, long timeout) {
        return getDeviceNodeId(((AbstractDeviceMessage) message).getDeviceId())   // ① 设备所在节点
                .flatMap(nodeId -> {
                    if (nodeId.equals(clusterManager.getCurrentNodeId())) {        // ② 本节点
                        return localSender.sendAndWait(message, timeout);
                    }
                    // ③ 远程节点 → 代理调用（目标节点 DefaultDeviceMessageSendService → LocalDeviceMessageSender）
                    return clusterManager.getService(nodeId, DeviceMessageSendService.class)
                            .switchIfEmpty(Mono.error(new BusinessException("目标节点未注册发送服务: " + nodeId)))
                            .flatMap(service -> service.sendAndWait(message, timeout));
                });
    }

    @Override
    public Mono<Void> send(DeviceMessage message) {
        return getDeviceNodeId(((AbstractDeviceMessage) message).getDeviceId())
                .flatMap(nodeId -> {
                    if (nodeId.equals(clusterManager.getCurrentNodeId())) {
                        return localSender.send(message);
                    }
                    // 异步下发远程执行：目标节点只发不等回复，本节点直接返回（回复不跨节点回传）
                    return clusterManager.getService(nodeId, DeviceMessageSendService.class)
                            .switchIfEmpty(Mono.error(new BusinessException("目标节点未注册发送服务: " + nodeId)))
                            .flatMap(service -> service.send(message));
                });
    }

    /**
     * 设备所在节点 ID — Redis CONNECTION_SERVER_ID（会话注册时写入，注销时清除）。
     * 兜底：设备刚连接/上线瞬时，会话已在本节点注册但 Redis 字段尚未写入 — 取不到节点 ID 时查本节点会话：
     * 在线（该窗口内设备必然在本节点）→ 本节点下发；本节点也无会话 → 设备未在线，明确报错（不静默丢弃）
     */
    private Mono<String> getDeviceNodeId(String deviceId) {
        return registry.getDevice(deviceId)
                .flatMap(DeviceOperator::getConnectionServerId)
                .filter(StringUtils::isNotEmpty)
                .switchIfEmpty(sessionManager.getSession(deviceId, true)
                        .filter(DeviceSession::isAlive)
                        .map(session -> clusterManager.getCurrentNodeId())
                        .switchIfEmpty(Mono.error(new BusinessException("设备未在线，无法下发命令"))));
    }
}
