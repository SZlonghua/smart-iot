package net.lab1024.sa.base.module.support.protocol.mqtt.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * MQTT 已编码消息默认实现 — 持有 vertx MqttPublishMessage。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
@Getter
public class DefaultMqttEncodedMessage implements MqttEncodedMessage {

    private String topic;
    private String deviceId;
    private String clientId;
    private int qosLevel;
    private ByteBuf payload;
    private int messageId;
    private boolean will;
    private boolean dup;
    private boolean retain;
    /** 延迟 ACK 回调 — 由 VertxMqttConnection 注入（需要 endpoint 才能回 PUBACK/PUBREC），网关解码成功后调用 */
    @Setter
    private transient Runnable ack;

    public DefaultMqttEncodedMessage(String deviceId, String clientId, MqttPublishMessage publishMessage) {
        this.deviceId = deviceId;
        this.topic = publishMessage.topicName();
        this.clientId = clientId;
        this.qosLevel = publishMessage.qosLevel().value();
        this.payload = Unpooled.wrappedBuffer(publishMessage.payload().getBytes());
        this.messageId = publishMessage.messageId();
        this.will = false;
        this.dup = publishMessage.isDup();
        this.retain = publishMessage.isRetain();
    }

    @Builder
    public DefaultMqttEncodedMessage(String deviceId,
                                     String topic,
                                     String clientId,
                                     int qosLevel,
                                     ByteBuf payload,
                                     int messageId,
                                     boolean will,
                                     boolean dup,
                                     boolean retain
    ) {
        this.deviceId = deviceId;
        this.topic = topic;
        this.clientId = clientId;
        this.qosLevel = qosLevel;
        this.payload = payload;
        this.messageId = messageId;
        this.will = will;
        this.dup = dup;
        this.retain = retain;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public int getQos() {
        return qosLevel;
    }

    @Override
    public boolean isWill() {
        return will;
    }

    @Override
    public void acknowledge() {
        if (ack != null) {
            ack.run();
        }
    }
}
