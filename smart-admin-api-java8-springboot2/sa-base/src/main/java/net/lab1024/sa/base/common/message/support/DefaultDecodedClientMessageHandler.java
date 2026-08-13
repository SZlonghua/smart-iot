package net.lab1024.sa.base.common.message.support;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.message.DecodedClientMessageHandler;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * 默认解码消息处理器。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
@Slf4j
public class DefaultDecodedClientMessageHandler implements DecodedClientMessageHandler {

    @Resource
    private IEventBus eventBus;

    @Override
    public Mono<Void> handle(Message message) {
        // Reply 类消息 → 回复到原始请求方
        // TODO: 实现 Reply 消息回写逻辑（MessageReply / RepayableMessage 类型待落地）
        // Child 子设备消息 → 解包后重新发布
        // TODO: 实现子设备消息解包逻辑（ChildDeviceMessage 类型待落地）
        // 普通消息 → 发布到事件总线
        eventBus.publishAsync(message);
        return Mono.empty();
    }
}
