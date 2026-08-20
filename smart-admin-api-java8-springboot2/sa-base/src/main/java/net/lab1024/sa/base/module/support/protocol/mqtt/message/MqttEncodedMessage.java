package net.lab1024.sa.base.module.support.protocol.mqtt.message;

import net.lab1024.sa.base.common.message.raw.EncodedMessage;

/**
 * MQTT 已编码消息。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
public interface MqttEncodedMessage extends EncodedMessage {

    String getClientId();

    int getMessageId();

    String getTopic();

    int getQos();

    boolean isWill();

    default boolean isDup() {
        return false;
    }

    default boolean isRetain() {
        return false;
    }

    /**
     * 延迟 ACK — 解码成功后由网关调用（QoS1 回 PUBACK / QoS2 回 PUBREC），
     * 解码失败则不 ack 并由网关断开连接，客户端超时重发
     */
    default void acknowledge() {
    }
}
