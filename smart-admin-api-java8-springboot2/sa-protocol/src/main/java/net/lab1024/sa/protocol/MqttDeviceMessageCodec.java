package net.lab1024.sa.protocol;

import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.common.message.codec.*;
import net.lab1024.sa.base.common.message.raw.EncodedMessage;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class MqttDeviceMessageCodec implements DeviceMessageCodec {
    @Override
    public Transport getSupportTransport() {
        return DefaultTransport.MQTT;
    }

    @NotNull
    @Override
    public Publisher<? extends Message> decode(@NotNull MessageDecodeContext context) {
        return Mono.empty();
    }

    @NotNull
    @Override
    public Publisher<? extends EncodedMessage> encode(@NotNull MessageEncodeContext context) {
        return Mono.empty();
    }
}
