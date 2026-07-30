package net.lab1024.sa.base.common.protocol;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProtocolSupportManager {

    void init();

    Mono<ProtocolSupport> getProtocol(String protocol);

    Flux<ProtocolSupport> getProtocols();

    Mono<Void> register(Mono<ProtocolSupport> protocolSupport);

}
