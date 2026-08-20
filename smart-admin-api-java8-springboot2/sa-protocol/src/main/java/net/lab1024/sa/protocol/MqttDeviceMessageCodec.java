package net.lab1024.sa.protocol;

import io.netty.buffer.Unpooled;
import net.lab1024.sa.base.common.message.DeviceMessage;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.common.message.codec.*;
import net.lab1024.sa.base.common.message.raw.EncodedMessage;
import net.lab1024.sa.base.common.topic.TopicMessageCodec;
import net.lab1024.sa.base.common.topic.TopicPayload;
import net.lab1024.sa.base.module.support.protocol.mqtt.message.DefaultMqttEncodedMessage;
import net.lab1024.sa.base.module.support.protocol.mqtt.message.MqttEncodedMessage;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * MQTT 设备消息编解码器 — 薄封装，编解码全部委托给 {@link TopicMessageCodec} 枚举。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
public class MqttDeviceMessageCodec implements DeviceMessageCodec {

    @Override
    public Transport getSupportTransport() {
        return DefaultTransport.MQTT;
    }

    @NotNull
    @Override
    public Publisher<? extends Message> decode(@NotNull MessageDecodeContext context) {
        // 数据库设备 ID 回填由网关层完成：直连消息取连接 deviceId，子设备消息查子设备会话缓存
        return Mono.fromCallable(() -> {
            MqttEncodedMessage encoded = (MqttEncodedMessage) context.getMessage();
            return TopicMessageCodec.decode(encoded.getTopic(), encoded.payloadAsBytes());
        }).filter(Objects::nonNull);
    }

    @NotNull
    @Override
    public Publisher<? extends EncodedMessage> encode(@NotNull MessageEncodeContext context) {
        return Mono.fromCallable(() -> {
            DeviceMessage message = (DeviceMessage) context.getMessage();
            TopicPayload payload = TopicMessageCodec.encode(message);
            return DefaultMqttEncodedMessage.builder()
                    .deviceId(message.getDeviceId())
                    .topic(payload.getTopic())
                    .payload(Unpooled.wrappedBuffer(payload.getPayload()))
                    .qosLevel(1)
                    .build();
        });
    }
}
