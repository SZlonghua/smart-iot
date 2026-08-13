package net.lab1024.sa.base.common.message.codec;

import net.lab1024.sa.base.common.message.Message;
import org.reactivestreams.Publisher;

import javax.annotation.Nonnull;

/**
 * 设备消息解码器。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
public interface DeviceMessageDecoder {

    @Nonnull
    Publisher<? extends Message> decode(@Nonnull MessageDecodeContext context);
}
