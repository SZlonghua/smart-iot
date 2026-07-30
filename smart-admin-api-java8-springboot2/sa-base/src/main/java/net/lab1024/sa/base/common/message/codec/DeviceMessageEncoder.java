package net.lab1024.sa.base.common.message.codec;

import net.lab1024.sa.base.common.message.raw.EncodedMessage;
import org.reactivestreams.Publisher;

import javax.annotation.Nonnull;

public interface DeviceMessageEncoder {

    @Nonnull
    Publisher<? extends EncodedMessage> encode(@Nonnull MessageEncodeContext context);
}
