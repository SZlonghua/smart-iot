package net.lab1024.sa.base.common.protocol;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.lab1024.sa.base.common.defaults.Authenticator;
import net.lab1024.sa.base.common.message.codec.DeviceMessageCodec;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.device.AuthenticationRequest;
import net.lab1024.sa.base.device.AuthenticationResponse;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


@Getter
@Setter
public class CompositeProtocolSupport implements ProtocolSupport {
    private String id;

    private String name;

    private String description;

    @Getter(AccessLevel.PRIVATE)
    private final Map<String, Supplier<Mono<DeviceMessageCodec>>> messageCodecSupports = new ConcurrentHashMap<>();


    @Getter(AccessLevel.PRIVATE)
    private Map<String, Authenticator> authenticators = new ConcurrentHashMap<>();

    @Override
    public Flux<? extends Transport> getSupportedTransport() {
        return Flux.fromIterable(messageCodecSupports.values())
                .flatMap(Supplier::get)
                .map(DeviceMessageCodec::getSupportTransport)
                .distinct(Transport::getId);
    }

    @NotNull
    @Override
    public Mono<? extends DeviceMessageCodec> getMessageCodec(Transport transport) {
        return messageCodecSupports.getOrDefault(transport.getId(), Mono::empty).get();
    }



    @Override
    public void addMessageCodec(Transport transport, DeviceMessageCodec codec) {
        messageCodecSupports.put(transport.getId(), () -> Mono.just(codec));
    }

    @Override
    public void addAuthenticator(Transport transport, Authenticator authenticator) {
        authenticators.put(transport.getId(), authenticator);
    }

    @NotNull
    @Override
    public Mono<AuthenticationResponse> authenticate(@NotNull AuthenticationRequest request, @NotNull DeviceOperator deviceOperation) {
        return Mono.justOrEmpty(authenticators.get(request.getTransport().getId()))
                .flatMap(at -> at
                        .authenticate(request, deviceOperation)
                        .defaultIfEmpty(AuthenticationResponse.error(400, "无法获取认证结果")))
                .switchIfEmpty(Mono.error(() -> new UnsupportedOperationException("不支持的认证请求:" + request)));
    }

    @NotNull
    @Override
    public Mono<AuthenticationResponse> authenticate(@NotNull AuthenticationRequest request, @NotNull DeviceRegistry deviceRegistry) {
        return Mono.justOrEmpty(authenticators.get(request.getTransport().getId()))
                .flatMap(at -> at
                        .authenticate(request, deviceRegistry)
                        .defaultIfEmpty(AuthenticationResponse.error(400, "无法获取认证结果")))
                .switchIfEmpty(Mono.error(() -> new UnsupportedOperationException("不支持的认证请求:" + request)));
    }

    @Override
    public void dispose() {
        messageCodecSupports.clear();
    }

}
