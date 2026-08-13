package net.lab1024.sa.base.module.support.eventbus.core;

import org.springframework.core.ResolvableType;

/**
 * 事件处理器 — 通过匿名子类携带完整泛型事件类型。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
public abstract class EventHandler<T> {

    private final ResolvableType eventType;

    protected EventHandler() {
        this.eventType = ResolvableType.forClass(getClass()).as(EventHandler.class).getGeneric(0);
    }

    public ResolvableType getEventType() {
        return eventType;
    }

    public abstract void handle(T event);
}
