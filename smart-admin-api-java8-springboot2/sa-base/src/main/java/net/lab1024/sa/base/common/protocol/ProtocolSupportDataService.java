package net.lab1024.sa.base.common.protocol;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProtocolSupportDataService {

    Flux<ProtocolSupportDefinition> getAll();

    Mono<ProtocolSupportDefinition> getById(String id);
}
