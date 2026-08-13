package net.lab1024.sa.base.module.support.eventbus.core;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * 从事件对象中提取 ResolvableType。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
public class ResolvableTypeUtil {

    public static ResolvableType resolve(Object event) {
        if (event instanceof ResolvableTypeProvider) {
            return ((ResolvableTypeProvider) event).getResolvableType();
        }
        return ResolvableType.forInstance(event);
    }
}
