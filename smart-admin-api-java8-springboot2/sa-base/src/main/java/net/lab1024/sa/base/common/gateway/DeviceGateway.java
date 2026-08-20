package net.lab1024.sa.base.common.gateway;

import net.lab1024.sa.base.common.message.Message;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;

public interface DeviceGateway {
    /**
     * @return 网关ID
     */
    String getId();

    /**
     * 订阅来自设备到消息,关闭网关时不会结束流.
     *
     * @return 设备消息流
     */
//    Flux<Message> onMessage();

    /**
     * 启动网关
     *
     * @return 启动结果
     */
    Mono<Void> startup();

    /**
     * 暂停网关,暂停后停止处理设备消息.
     *
     * @return 暂停结果
     */
    Mono<Void> pause();

    /**
     * 关闭网关
     *
     * @return 关闭结果
     */
    Mono<Void> shutdown();

    default boolean isAlive() {
        return true;
    }

    default boolean isStarted() {
        return getState() == GatewayState.started;
    }

    default GatewayState getState() {
        return GatewayState.started;
    }

    default void doOnStateChange(BiConsumer<GatewayState, GatewayState> listener) {

    }

    default void doOnShutdown(Disposable disposable) {
        doOnStateChange((before, after) -> {
            if (after == GatewayState.shutdown) {
                disposable.dispose();
            }
        });
    }

    default boolean isChangeNetwork(String networkId){
        return false;
    }

    default boolean isChangeProtocol(String protocol){
        return false;
    }

}
