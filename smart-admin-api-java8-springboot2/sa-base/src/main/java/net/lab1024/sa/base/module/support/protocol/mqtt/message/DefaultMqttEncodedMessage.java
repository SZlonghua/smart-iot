package net.lab1024.sa.base.module.support.protocol.mqtt.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.Builder;
import lombok.Getter;

/**
 * MQTT 已编码消息默认实现 — 持有 vertx MqttPublishMessage。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
@Getter
@Builder
public class DefaultMqttEncodedMessage implements MqttEncodedMessage {

    private String topic;
    private String clientId;
    private int qosLevel;
    private ByteBuf payload;
    private int messageId;
    private boolean will;
    private boolean dup;
    private boolean retain;

    public DefaultMqttEncodedMessage(String clientId, MqttPublishMessage publishMessage) {
        this.topic = publishMessage.topicName();
        this.clientId = clientId;
        this.qosLevel = publishMessage.qosLevel().value();
        this.payload = Unpooled.wrappedBuffer(publishMessage.payload().getBytes());
        this.messageId = publishMessage.messageId();
        this.will = false;
        this.dup = publishMessage.isDup();
        this.retain = publishMessage.isRetain();
    }

    public DefaultMqttEncodedMessage(String topic,
                                     String clientId,
                                     int qosLevel,
                                     ByteBuf payload,
                                     int messageId,
                                     boolean will,
                                     boolean dup,
                                     boolean retain
    ) {
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
}
