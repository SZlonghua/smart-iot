package net.lab1024.sa.base.module.support.protocol.mqtt.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.mqtt.MqttWill;
import org.jetbrains.annotations.NotNull;

public class WillMqttEncodedMessage implements MqttEncodedMessage {

    private final String clientId;
    private final MqttWill delegate;

    public WillMqttEncodedMessage(String clientId, MqttWill delegate) {
        this.clientId = clientId;
        this.delegate = delegate;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public int getMessageId() {
        return 0;
    }

    @Override
    public String getTopic() {
        return delegate.getWillTopic();
    }

    @Override
    public int getQos() {
        return delegate.getWillQos();
    }

    @Override
    public boolean isWill() {
        return true;
    }

    @NotNull
    @Override
    public ByteBuf getPayload() {
        return Unpooled.wrappedBuffer(delegate.getWillMessageBytes());
    }

}
