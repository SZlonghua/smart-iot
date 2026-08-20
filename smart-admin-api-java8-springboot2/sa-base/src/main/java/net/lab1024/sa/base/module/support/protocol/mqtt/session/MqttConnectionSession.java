package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import lombok.Getter;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;

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

    public MqttConnectionSession(MqttConnection connection, DeviceOperator deviceOperator, Transport transport,
                                 DeviceSessionManager sessionManager, String gatewayId) {
        super(connection.getDeviceId(), deviceOperator, transport,
                connection.getProductKey(), connection.getDeviceKey(), gatewayId);
        this.connection = connection;
        // 连接关闭 → 移除自己；predicate 校验会话仍是本实例，新连接替换后不误删新会话
        connection.onClose(closed -> sessionManager.remove(getDeviceId(), session -> session == this).subscribe());
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
}
