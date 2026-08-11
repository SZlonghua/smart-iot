package net.lab1024.sa.protocol;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.message.codec.DeviceMessageCodec;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.defaults.Authenticator;
import net.lab1024.sa.base.device.AuthenticationRequest;
import net.lab1024.sa.base.device.AuthenticationResponse;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 演示用协议支持。
 *
 * @Author 廖涛
 * @Date 2026/08/11
 * @Copyright 1024创新实验室
 */
@Slf4j
public class SmartIoTProtocolSupport implements ProtocolSupport {

    @Nonnull
    @Override
    public String getId() {
        return "demo-protocol";
    }

    @Override
    public String getName() {
        return "演示协议";
    }

    @Override
    public String getDescription() {
        return "用于本地开发调试的演示协议";
    }

    @Override
    public Flux<? extends Transport> getSupportedTransport() {
        return Flux.empty();
    }

    @Nonnull
    @Override
    public Mono<? extends DeviceMessageCodec> getMessageCodec(Transport transport) {
        return Mono.empty();
    }

    @Override
    public void addAuthenticator(Transport transport, Authenticator authenticator) {
    }

    @Override
    public void addMessageCodec(Transport transport, DeviceMessageCodec codec) {
    }

    @Nonnull
    @Override
    public Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request, @Nonnull DeviceOperator deviceOperation) {
        return Mono.just(AuthenticationResponse.success());
    }

    @Nonnull
    @Override
    public Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request, @Nonnull DeviceRegistry deviceRegistry) {
        return Mono.just(AuthenticationResponse.success());
    }

    @Override
    public void dispose() {
        log.info("[DemoProtocol] dispose");
    }
}
