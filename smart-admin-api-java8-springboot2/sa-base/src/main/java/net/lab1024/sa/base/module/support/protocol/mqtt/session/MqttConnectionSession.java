package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import lombok.Getter;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.common.message.raw.EncodedMessage;
import reactor.core.publisher.Mono;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * MQTT Broker 侧设备连接会话 — 平台作为 MQTT Broker 接受设备连接。
 * <p>
 * 连接关闭时（断连/异常/被替换）自动从会话管理器移除自己。
 *
 * &#064;Author  廖涛
 * &#064;Date  2026/07/27
 * &#064;Copyright  1024创新实验室
 */
@Getter
public class MqttConnectionSession extends AbstractDeviceSession {

    private final MqttConnection connection;

    /** 协议实例 ID（ProtocolSupport.getId()，如 "mqtt"）— 会话注册时写入 Redis 设备字段 */
    @Nullable
    private final String protocolId;

    /** 连接服务器节点 ID（集群节点 ID）— 会话注册时写入 Redis 设备字段；单机部署为 null */
    @Nullable
    private final String connectionServerId;

    /** 会话注销成功回调（连接关闭移除会话后）— 由创建方传入，用于发布设备下线消息 */
    private final Consumer<DeviceSession> onUnregister;

    public MqttConnectionSession(MqttConnection connection, DeviceOperator deviceOperator, Transport transport,
                                 DeviceSessionManager sessionManager, String gatewayId,
                                 @Nullable String protocolId, @Nullable String connectionServerId,
                                 Consumer<DeviceSession> onUnregister) {
        super(connection.getDeviceId(), deviceOperator, transport,
                connection.getProductKey(), connection.getDeviceKey(), gatewayId);
        this.connection = connection;
        this.protocolId = protocolId;
        this.connectionServerId = connectionServerId;
        this.onUnregister = onUnregister;
        // 连接关闭 → 移除自己（移除成功回调 onUnregister — 发布设备下线消息）；
        // predicate 校验会话仍是本实例，新连接替换后不误删新会话
        connection.onClose(closed -> sessionManager.remove(getDeviceId(), session -> session == this,
                onUnregister).subscribe());
    }

    @Override
    public long lastPingTime() {
        return connection.getLastPingTime();
    }

    @Override
    public void close() {
        connection.close();
        // 触发关闭监听器 — 子设备会话级联移除自己
        notifyClose();
    }

    @Override
    public boolean isAlive() {
        return connection != null && connection.isAlive();
    }

    /** 发送已编码的消息 — 转发到 MQTT 连接（下线时返回 false） */
    @Override
    public Mono<Boolean> send(EncodedMessage encodedMessage) {
        if (!isAlive()) {
            return Mono.just(false);
        }
        return connection.sendMessage(encodedMessage).thenReturn(true);
    }
}
