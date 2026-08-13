package net.lab1024.sa.base.module.support.eventbus.impl;

import net.lab1024.sa.base.module.support.eventbus.core.Subscription;
import org.springframework.core.ResolvableType;

import java.util.function.Consumer;

/**
 * 注册条目实现。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
class SubscriptionEntry<T> implements Subscription {

    private final ResolvableType eventType;
    private final Consumer<T> handler;
    private final SpringEventBus bus;
    private volatile boolean active = true;

    SubscriptionEntry(ResolvableType eventType, Consumer<T> handler, SpringEventBus bus) {
        this.eventType = eventType;
        this.handler = handler;
        this.bus = bus;
    }

    @Override
    public ResolvableType getEventType() { return eventType; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void cancel() {
        this.active = false;
        this.bus.unsubscribe(this);
    }

    Consumer<T> getHandler() { return handler; }

    boolean matches(ResolvableType publishedType) {
        if (!eventType.hasGenerics()) {
            return true;
        }
        return eventType.isAssignableFrom(publishedType);
    }
}
