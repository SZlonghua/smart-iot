package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;
import net.lab1024.sa.base.message.codec.DefaultTransport;

/**
 * MQTT Broker 侧设备连接会话 — 平台作为 MQTT Broker 接受设备连接。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
public class MqttConnectionSession extends AbstractDeviceSession {

    public MqttConnectionSession(String deviceId) {
        super(deviceId, DefaultTransport.MQTT);
    }

    @Override
    public boolean isAlive() {
        return !closed;
    }
}
