package net.lab1024.sa.base.common.message.support;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.message.AbstractDeviceMessageReply;
import net.lab1024.sa.base.common.message.DecodedClientMessageHandler;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.device.DeviceMessageReplyHandler;
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
 * - 注册（上线）：缓存 SESSION_ID + ONLINE_TIME + GATEWAY_ID + PROTOCOL_ID + CONNECTION_SERVER_ID 到设备存储（Redis Hash），并回调持久化更新数据库
 * - 注销（下线）：清除上述字段 + 缓存 OFFLINE_TIME，并回调持久化更新数据库
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
    private final DeviceMessageReplyHandler deviceMessageReplyHandler;

    public DefaultDecodedClientMessageHandler(IEventBus eventBus,
                                              DeviceSessionManager sessionManager,
                                              DeviceOnlineStatePersistence onlineStatePersistence,
                                              DeviceMessageReplyHandler deviceMessageReplyHandler) {
        this.eventBus = eventBus;
        this.onlineStatePersistence = onlineStatePersistence;
        this.deviceMessageReplyHandler = deviceMessageReplyHandler;
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

    /** 下线：删除 SESSION_ID/GATEWAY_ID/PARENT_DEVICE_ID/PROTOCOL_ID/CONNECTION_SERVER_ID 字段（置空串不算清除）+ 缓存 OFFLINE_TIME，持久化离线状态 */
    private void handleUnregister(DeviceSession session) {
        DeviceOperator operator = session.getOperator();
        if (operator != null) {
            operator.removeConfigs(DeviceField.SESSION_ID.getValue(),
                    DeviceField.GATEWAY_ID.getValue(), DeviceField.PARENT_DEVICE_ID.getValue(),
                    DeviceField.PROTOCOL_ID.getValue(), DeviceField.CONNECTION_SERVER_ID.getValue());
            operator.setConfigs(Collections.singletonMap(
                    DeviceField.OFFLINE_TIME.getValue(), System.currentTimeMillis()));
        }
        if (onlineStatePersistence != null) {
            onlineStatePersistence.onOffline(session);
        }
    }

    /**
     * 构建上线缓存配置：SESSION_ID + ONLINE_TIME + GATEWAY_ID（网关实例ID）+ PROTOCOL_ID（协议实例ID）+ CONNECTION_SERVER_ID（集群节点ID，单机不写），
     * 子设备额外缓存 PARENT_DEVICE_ID（父设备ID）
     */
    private Map<String, Object> registerConfigs(DeviceSession session) {
        Map<String, Object> configs = new HashMap<>(6);
        configs.put(DeviceField.SESSION_ID.getValue(), session.getId());
        configs.put(DeviceField.ONLINE_TIME.getValue(), System.currentTimeMillis());
        if (session.getGatewayId() != null) {
            configs.put(DeviceField.GATEWAY_ID.getValue(), session.getGatewayId());
        }
        if (session.getProtocolId() != null) {
            configs.put(DeviceField.PROTOCOL_ID.getValue(), session.getProtocolId());
        }
        if (session.getConnectionServerId() != null) {
            configs.put(DeviceField.CONNECTION_SERVER_ID.getValue(), session.getConnectionServerId());
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
        // ① 回复类消息 → 回调回复处理者（LocalDeviceMessageSender.onReply），按 messageId 完成发送方的 pending 等待
        //    （子设备回复 ChildDeviceMessageReply 在 onReply 内解包后下发内层回复 — 见 3.5；事件总线发布保持外层，见 ②）
        if (message instanceof AbstractDeviceMessageReply) {
            deviceMessageReplyHandler.onReply((AbstractDeviceMessageReply) message);
        }
        // ② 普通消息 → 发布到事件总线（回复消息同样发布，业务可监听；子设备消息按外层发布，监听器经
        //    ResolvableTypeProvider 泛型匹配内层类型，需要内层内容时自行解包 getChildDeviceMessage()）
        eventBus.publishAsync(message);
        return Mono.empty();
    }

}
