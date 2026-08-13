package net.lab1024.sa.base.common.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 网络组件配置事件。
 *
 * @Author 廖涛
 * @Date 2026/08/01
 * @Copyright 1024创新实验室
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class NetworkConfigEvent {

    private final Type type;
    private final NetworkProperties properties;

    public enum Type {
        reload,
        destroy
    }
}
