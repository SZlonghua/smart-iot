package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;

/**
 * MQTT Client 侧连接会话 — 平台作为 MQTT Client 连接外部 Broker（如 EMQX）。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
public class MqttClientSession extends AbstractDeviceSession {

    public MqttClientSession(String deviceId) {
        super(deviceId, DefaultTransport.MQTT);
    }

    @Override
    public boolean isAlive() {
        return !closed;
    }
}
