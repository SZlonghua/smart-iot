package net.lab1024.sa.base.module.support.protocol.gateway;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.DeviceGateway;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.GatewayState;
import net.lab1024.sa.base.common.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiConsumer;

/**
 * 设备网关抽象基类 — 提供公共字段和默认实现。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
@Slf4j
public abstract class AbstractDeviceGateway implements DeviceGateway {

    protected final DeviceGatewayProperties properties;
    protected volatile GatewayState state = GatewayState.shutdown;
    private final static AtomicReferenceFieldUpdater<AbstractDeviceGateway, GatewayState>
            STATE = AtomicReferenceFieldUpdater.newUpdater(AbstractDeviceGateway.class, GatewayState.class, "state");


    private final List<BiConsumer<GatewayState, GatewayState>> stateListener = new CopyOnWriteArrayList<>();


    protected AbstractDeviceGateway(DeviceGatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public GatewayState getState() {
        return state;
    }

    @Override
    public Flux<Message> onMessage() {
        return Flux.empty();
    }

    @Override
    public Mono<Void> startup() {
        if (state == GatewayState.paused) {
            changeState(GatewayState.started);
            return Mono.empty();
        }
        if (state == GatewayState.started || state == GatewayState.starting) {
            return Mono.empty();
        }
        changeState(GatewayState.starting);
        return this
                .doStartup()
                .doOnSuccess(ignore -> changeState(GatewayState.started));
    }

    protected synchronized final void changeState(GatewayState target) {
        GatewayState old = STATE.getAndSet(this, target);
        if (target == old) {
            return;
        }
        for (BiConsumer<GatewayState, GatewayState> consumer : stateListener) {
            try {
                consumer.accept(old, this.state);
            } catch (Throwable error) {
                log.warn("fire gateway {} state listener error", getId(), error);
            }
        }
    }

    @Override
    public Mono<Void> pause() {
        changeState(GatewayState.paused);
        return Mono.empty();
    }

    @Override
    public Mono<Void> shutdown() {
        GatewayState old = STATE.getAndSet(this, GatewayState.shutdown);

        if (old == GatewayState.shutdown) {
            return Mono.empty();
        }
        changeState(GatewayState.shutdown);
        return doShutdown();
    }

    /** 子类实现：启动逻辑 */
    protected abstract Mono<Void> doStartup();

    /** 子类实现：关闭逻辑 */
    protected abstract Mono<Void> doShutdown();

    @Override
    public final void doOnStateChange(BiConsumer<GatewayState, GatewayState> listener) {
        stateListener.add(listener);
    }

    @Override
    public boolean isAlive() {
        return state == GatewayState.started || state == GatewayState.starting;
    }
}
