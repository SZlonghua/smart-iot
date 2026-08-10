package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import lombok.Getter;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;

/**
 * MQTT Broker 侧设备连接会话 — 平台作为 MQTT Broker 接受设备连接。
 *
 * &#064;Author  廖涛
 * &#064;Date  2026/07/27
 * &#064;Copyright  1024创新实验室
 */
@Getter
public class MqttConnectionSession extends AbstractDeviceSession {

    private final MqttConnection connection;

    public MqttConnectionSession(MqttConnection connection, DeviceOperator deviceOperator, Transport transport) {
        super(connection.getDeviceId(), deviceOperator, transport);
        this.connection = connection;
    }

    @Override
    public long lastPingTime() {
        return connection.getLastPingTime();
    }

    @Override
    public void close() {
        connection.close();
    }

    @Override
    public boolean isAlive() {
        return connection != null && connection.isAlive();
    }
}
