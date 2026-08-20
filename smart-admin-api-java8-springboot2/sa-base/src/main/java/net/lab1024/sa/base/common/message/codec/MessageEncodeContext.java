package net.lab1024.sa.base.common.message.codec;

import net.lab1024.sa.base.common.message.DeviceMessage;
import net.lab1024.sa.base.common.message.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface MessageEncodeContext extends MessageCodecContext {

    @Nonnull
    Message getMessage();


    /**
     * 直接回复消息给平台.在类似通过http接入时,下发指令可能是一个同步操作,则可以通过此方法直接回复平台.
     *
     * @param replyMessage 消息流
     * @return void
     * @since 1.0.2
     */
    @Nonnull
    default Mono<Void> reply(@Nonnull Publisher<? extends DeviceMessage> replyMessage) {
        return Mono.empty();
    }

    /**
     * {@link MessageEncodeContext#reply(Publisher)}
     *
     * @param messages 消息
     * @return void
     * @since 1.1.1
     */
    @Nonnull
    default Mono<Void> reply(@Nonnull Collection<? extends DeviceMessage> messages) {
        return reply(Flux.fromIterable(messages));
    }

    /**
     * {@link MessageEncodeContext#reply(Publisher)}
     *
     * @param messages 消息
     * @return void
     * @since 1.1.1
     */
    @Nonnull
    default Mono<Void> reply(@Nonnull DeviceMessage... messages) {
        return reply(Flux.fromArray(messages));
    }
}
