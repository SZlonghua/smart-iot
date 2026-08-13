package net.lab1024.sa.base.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 协议生命周期事件。
 *
 * @Author 廖涛
 * @Date 2026/07/30
 * @Copyright 1024创新实验室
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ProtocolSupportEvent {

    private final Type type;
    private final ProtocolSupportDefinition definition;

    public enum Type {
        SAVED,
        UPDATED,
        DELETED
    }
}
