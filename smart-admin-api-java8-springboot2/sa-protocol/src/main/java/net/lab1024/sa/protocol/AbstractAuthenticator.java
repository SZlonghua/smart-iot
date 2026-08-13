package net.lab1024.sa.protocol;

import net.lab1024.sa.base.common.defaults.Authenticator;
import net.lab1024.sa.base.device.AuthenticationRequest;
import net.lab1024.sa.base.device.AuthenticationResponse;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.module.support.protocol.mqtt.session.MqttAuthenticationRequest;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class AbstractAuthenticator implements Authenticator {

    @Override
    public Mono<AuthenticationResponse> authenticate(@NotNull AuthenticationRequest request, @NotNull DeviceOperator device) {
        return device.authenticate(((MqttAuthenticationRequest) request).toDeviceRequest());
    }

    @Override
    public Mono<AuthenticationResponse> authenticate(@NotNull AuthenticationRequest request, @NotNull DeviceRegistry registry) {
        MqttAuthenticationRequest mqttReq = (MqttAuthenticationRequest) request;
        return Mono.fromCallable(mqttReq::toDeviceRequest)
                .flatMap(deviceReq -> registry.getDevice(deviceReq.getProductKey(), deviceReq.getDeviceKey())
                        .flatMap(device -> authenticate(request, device)))
                .onErrorResume(e -> Mono.just(AuthenticationResponse.error(400, e.getMessage())));
    }
}
