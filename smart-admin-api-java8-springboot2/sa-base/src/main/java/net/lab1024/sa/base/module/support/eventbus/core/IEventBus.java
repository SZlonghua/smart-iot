package net.lab1024.sa.base.module.support.eventbus.core;

import java.util.function.Consumer;

/**
 * 动态事件总线 — 支持运行时注册/注销事件处理器，支持泛型类型匹配。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
public interface IEventBus {

    <T> Subscription subscribe(Class<T> eventType, Consumer<T> handler);

    <T> Subscription subscribe(EventHandler<T> eventHandler);

    void unsubscribe(Subscription subscription);

    void publish(Object event);

    void publishAsync(Object event);
}
