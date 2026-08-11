package net.lab1024.sa.protocol;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.spi.ProtocolSupportProvider;
import net.lab1024.sa.base.common.spi.ServiceContext;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示用协议支持提供者。
 *
 * @Author 廖涛
 * @Date 2026/08/11
 * @Copyright 1024创新实验室
 */
@Slf4j
public class SmartIoTProtocolSupportProvider implements ProtocolSupportProvider {

    private AtomicInteger idCounter = new AtomicInteger(0);
    @Override
    public Mono<? extends ProtocolSupport> create(ServiceContext context) {
        log.info("[DemoProtocol] 创建协议支持{}", idCounter.incrementAndGet());
        return Mono.just(new SmartIoTProtocolSupport());
    }

    @Override
    public void dispose() {
        log.info("[DemoProtocol] dispose");
    }
}
