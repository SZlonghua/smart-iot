package net.lab1024.sa.base.common.network;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NetworkManager {

    /**
     * 根据组件类型和ID获取网络组件。
     */
    <T extends Network> Mono<T> getNetwork(NetworkType type, String id);

    /**
     * 获取全部网络组件
     *
     * @return 网络组件
     */
    Flux<Network> getNetworks();

    /**
     * 重新加载网络组件
     *
     * @param type 网络组件类型
     * @param id   ID
     * @return void
     */
    Mono<Void> reload(NetworkType type, String id);

    /**
     * 停止网络组件
     *
     * @param type 网络组件类型
     * @param id   ID
     * @return void
     */
    Mono<Void> shutdown(NetworkType type, String id);

    /**
     * 销毁网络组件
     *
     * @param type 网络组件类型
     * @param id   ID
     * @return void
     */
    Mono<Void> destroy(NetworkType type, String id);
}
