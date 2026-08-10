package net.lab1024.sa.base.common.message;

import reactor.core.publisher.Mono;

/**
 * 解码后的设备消息处理器。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
public interface DecodedClientMessageHandler {

    /**
     * 处理解码后的消息：
     * - Reply 类消息 → 回复到原始请求方
     * - ChildDevice/Child 子设备消息 → 解包后重新发布
     * - 普通消息 → 发布到事件总线
     */
    Mono<Void> handle(Message message);
}
