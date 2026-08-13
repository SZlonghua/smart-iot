package net.lab1024.sa.base.common.spi;

import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public interface ProtocolSupportProvider extends Disposable {

    /**
     * 创建协议支持
     *
     * @param context 上下文
     * @return 协议支持
     */
    Mono<? extends ProtocolSupport> create(ServiceContext context);

    /**
     * 关闭资源 如线程池，连接 没有就不管
     */
    @Override
    default void dispose() {

    }
}
