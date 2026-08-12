package net.lab1024.sa.base.common.defaults;

import net.lab1024.sa.base.device.AuthenticationRequest;
import net.lab1024.sa.base.device.AuthenticationResponse;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface Authenticator {

    /**
     * 对指定对设备进行认证
     *
     * @param request 认证请求
     * @param device  设备
     * @return 认证结果
     */
    Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request,
                                              @Nonnull DeviceOperator device);

    /**
     * 在网络连接建立的时候,可能无法获取设备的标识(如:http,websocket等),则会调用此方法来进行认证.
     * 注意: 认证通过后,需要设置设备ID.
     *
     * @param request  认证请求
     * @param registry 设备注册中心
     * @return 认证结果
     */
    default Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request,
                                                      @Nonnull DeviceRegistry registry) {
        return registry.getDevice(request.getProductKey(), request.getDeviceKey())
                .flatMap(device -> authenticate(request, device));
    }
}
