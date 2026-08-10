package net.lab1024.sa.base.module.support.protocol;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import net.lab1024.sa.base.common.defaults.Authenticator;
import net.lab1024.sa.base.common.message.codec.DeviceMessageCodec;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.device.AuthenticationRequest;
import net.lab1024.sa.base.device.AuthenticationResponse;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@AllArgsConstructor
@Generated
public class RenameProtocolSupport implements ProtocolSupport {

    @Getter
    private final String id;

    @Getter
    private final String name;

    @Getter
    private final String description;

    private final ProtocolSupport target;

    private final IEventBus eventBus;

    @Override
    public Flux<? extends Transport> getSupportedTransport() {
        return target.getSupportedTransport();
    }

    @NotNull
    @Override
    public Mono<? extends DeviceMessageCodec> getMessageCodec(Transport transport) {
        return target.getMessageCodec(transport);
    }

    @Override
    public void addAuthenticator(Transport transport, Authenticator authenticator) {
        target.addAuthenticator(transport, authenticator);
    }

    @Override
    public void addMessageCodec(Transport transport, DeviceMessageCodec codec) {
        target.addMessageCodec(transport,codec);
    }

    @NotNull
    @Override
    public Mono<AuthenticationResponse> authenticate(@NotNull AuthenticationRequest request, @NotNull DeviceOperator deviceOperation) {
        return target.authenticate(request, deviceOperation);
    }

    @NotNull
    @Override
    public Mono<AuthenticationResponse> authenticate(@NotNull AuthenticationRequest request, @NotNull DeviceRegistry deviceRegistry) {
        return target.authenticate(request, deviceRegistry);
    }
}
