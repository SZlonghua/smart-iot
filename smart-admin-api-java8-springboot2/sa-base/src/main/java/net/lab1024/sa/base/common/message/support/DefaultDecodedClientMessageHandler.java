package net.lab1024.sa.base.common.message.support;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.message.DecodedClientMessageHandler;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.DeviceSessionEvent;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.device.session.support.ChildDeviceSession;
import net.lab1024.sa.base.device.support.DeviceField;
import net.lab1024.sa.base.device.support.DeviceOnlineStatePersistence;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认解码消息处理器。
 * <p>
 * 构造时监听会话注册/注销事件：
 * - 注册（上线）：缓存 SESSION_ID + ONLINE_TIME 到设备存储（Redis Hash），并回调持久化更新数据库
 * - 注销（下线）：清除 SESSION_ID + 缓存 OFFLINE_TIME，并回调持久化更新数据库
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/07
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
public class DefaultDecodedClientMessageHandler implements DecodedClientMessageHandler {

    private final IEventBus eventBus;
    @Getter
    private final DeviceOnlineStatePersistence onlineStatePersistence;

    public DefaultDecodedClientMessageHandler(IEventBus eventBus,
                                              DeviceSessionManager sessionManager,
                                              DeviceOnlineStatePersistence onlineStatePersistence) {
        this.eventBus = eventBus;
        this.onlineStatePersistence = onlineStatePersistence;
        // 监听会话注册/注销 — 缓存在线状态 + 更新数据库 由online offline设备消息触发会话
        sessionManager.listenEvent(this::handleSessionEvent);
    }

    /** 会话注册/注销事件 — 缓存在线状态（Redis），并回调持久化更新数据库 */
    private Mono<Void> handleSessionEvent(DeviceSessionEvent event) {
        if (event.getType() == DeviceSessionEvent.Type.register) {
            handleRegister(event.getSession());
        } else {
            handleUnregister(event.getSession());
        }
        return Mono.empty();
    }

    /** 上线：缓存 SESSION_ID + ONLINE_TIME（子设备额外缓存 GATEWAY_ID），持久化在线状态 */
    private void handleRegister(DeviceSession session) {
        DeviceOperator operator = session.getOperator();
        if (operator != null) {
            operator.setConfigs(registerConfigs(session));
        }
        if (onlineStatePersistence != null) {
            onlineStatePersistence.onOnline(session);
        }
    }

    /** 下线：删除 SESSION_ID/GATEWAY_ID/PARENT_DEVICE_ID 字段（置空串不算清除）+ 缓存 OFFLINE_TIME，持久化离线状态 */
    private void handleUnregister(DeviceSession session) {
        DeviceOperator operator = session.getOperator();
        if (operator != null) {
            operator.removeConfigs(DeviceField.SESSION_ID.getValue(),
                    DeviceField.GATEWAY_ID.getValue(), DeviceField.PARENT_DEVICE_ID.getValue());
            operator.setConfigs(Collections.singletonMap(
                    DeviceField.OFFLINE_TIME.getValue(), System.currentTimeMillis()));
        }
        if (onlineStatePersistence != null) {
            onlineStatePersistence.onOffline(session);
        }
    }

    /**
     * 构建上线缓存配置：SESSION_ID + ONLINE_TIME + GATEWAY_ID（网关实例ID），
     * 子设备额外缓存 PARENT_DEVICE_ID（父设备ID）
     */
    private Map<String, Object> registerConfigs(DeviceSession session) {
        Map<String, Object> configs = new HashMap<>(4);
        configs.put(DeviceField.SESSION_ID.getValue(), session.getId());
        configs.put(DeviceField.ONLINE_TIME.getValue(), System.currentTimeMillis());
        if (session.getGatewayId() != null) {
            configs.put(DeviceField.GATEWAY_ID.getValue(), session.getGatewayId());
        }
        if (session instanceof ChildDeviceSession) {
            DeviceSession parent = ((ChildDeviceSession) session).getParent();
            if (parent != null) {
                configs.put(DeviceField.PARENT_DEVICE_ID.getValue(), parent.getDeviceId());
            }
        }
        return configs;
    }

    @Override
    public Mono<Void> handle(Message message) {
        log.info("handle message: {}", message);
        // Reply 类消息 → 回复到原始请求方
        // TODO: 实现 Reply 消息回写逻辑（MessageReply / RepayableMessage 类型待落地）
        // Child 子设备消息 → 解包后重新发布
        // TODO: 实现子设备消息解包逻辑（ChildDeviceMessage 类型待落地）
        // 普通消息 → 发布到事件总线
        eventBus.publishAsync(message);
        return Mono.empty();
    }

}
