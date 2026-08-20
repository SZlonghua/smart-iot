package net.lab1024.sa.base.common.message.codec;

import net.lab1024.sa.base.common.message.DeviceMessage;
import net.lab1024.sa.base.common.message.raw.EncodedMessage;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MessageDecodeContext extends MessageCodecContext {

    @Nonnull
    EncodedMessage getMessage();

    /*
      手动调用处理设备消息,此操作可以感知到设备消息的处理结果. 可通过{@link Mono#onErrorResume(Function)}来处理错误.
      <pre>{@code
     *
     *   DeviceMessage msg = doDecode(context);
     *
     *   return context
     *          .handleMessage(msg)
     *          //应答错误
     *          .onErrorResume(err-> return ackError(context).then(Mono.error(err)))
     *          //应答成功
     *          .then(ackSuccess(context))
     *          //返回空,因为handleMessage已经手动处理过了
     *          .then(Mono.empty())
     *
     * }
      @param message 设备消息
     * @see Mono#onErrorResume(Function)
     * @see Mono#doOnError(Consumer)
     * @return void
     * @since 1.2.2
     */
   /* default Mono<Void> handleMessage(DeviceMessage message) {
        return Mono.error(new UnsupportedOperationException("handleMessage not supported"));
    }*/
}
