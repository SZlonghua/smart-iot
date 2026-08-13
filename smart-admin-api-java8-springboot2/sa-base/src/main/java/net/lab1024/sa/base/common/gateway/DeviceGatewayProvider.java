package net.lab1024.sa.base.common.gateway;

import net.lab1024.sa.base.common.message.codec.Transport;
import reactor.core.publisher.Mono;

public interface DeviceGatewayProvider {

    String getId();

    /**
     * @return 名称
     */
    String getName();

    /**
     * @return 接入说明
     */
    default String getDescription() {
        return null;
    }

    /**
     * @return 排序。从小到大排序
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * @return 传输协议
     */
    Transport getTransport();

    /**
     * 使用配置信息创建设备网关
     *
     * @param properties 配置
     * @return void
     */
    Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties);

    /**
     * 重新加载网关
     *
     * @param gateway    网关
     * @param properties 配置信息
     * @return void
     */
    default Mono<? extends DeviceGateway> reloadDeviceGateway(DeviceGateway gateway, DeviceGatewayProperties properties) {
        return Mono.just(gateway);
    }
}
