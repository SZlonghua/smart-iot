package net.lab1024.sa.base.module.support.eventbus.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.module.support.eventbus.core.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Spring EventBus 实现。发布时同时通知动态 handler 和 Spring @EventListener。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Slf4j
public class SpringEventBus implements IEventBus {

    private final Map<Class<?>, List<SubscriptionEntry<?>>> subscribers = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher springPublisher;
    private final AsyncTaskExecutor asyncExecutor;

    public SpringEventBus(ApplicationEventPublisher springPublisher, AsyncTaskExecutor asyncExecutor) {
        this.springPublisher = springPublisher;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public <T> Subscription subscribe(Class<T> eventType, Consumer<T> handler) {
        return addSubscription(ResolvableType.forClass(eventType), handler);
    }

    @Override
    public <T> Subscription subscribe(EventHandler<T> eventHandler) {
        return addSubscription(eventHandler.getEventType(), eventHandler::handle);
    }

    private <T> Subscription addSubscription(ResolvableType eventType, Consumer<T> handler) {
        Class<?> rawType = eventType.resolve();
        SubscriptionEntry<T> entry = new SubscriptionEntry<>(eventType, handler, this);
        subscribers.computeIfAbsent(rawType, k -> new CopyOnWriteArrayList<>()).add(entry);
        return entry;
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        Class<?> rawType = subscription.getEventType().resolve();
        List<SubscriptionEntry<?>> list = subscribers.get(rawType);
        if (list != null) {
            list.removeIf(e -> e == subscription);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void publish(Object event) {
        ResolvableType publishedType = ResolvableTypeUtil.resolve(event);
        Class<?> rawType = publishedType.resolve();

        List<SubscriptionEntry<?>> list = subscribers.get(rawType);
        if (list != null) {
            for (SubscriptionEntry entry : list) {
                if (!entry.isActive()) continue;
                if (!entry.matches(publishedType)) continue;
                ((Consumer) entry.getHandler()).accept(event);
            }
        }
        if (springPublisher != null) {
            springPublisher.publishEvent(event);
        }
    }

    @Override
    public void publishAsync(Object event) {
        asyncExecutor.execute(() -> publish(event));
    }
}
