package net.lab1024.sa.base.module.support.eventbus.core;

import org.springframework.core.ResolvableType;

/**
 * 注册凭证 — subscribe() 的返回值。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
public interface Subscription {
    ResolvableType getEventType();
    boolean isActive();
    void cancel();
}
