package net.lab1024.sa.base.device.support;

import net.lab1024.sa.base.common.message.DeviceMessage;
import net.lab1024.sa.base.common.message.DeviceMessageReply;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.DeviceSendOperator;
import net.lab1024.sa.base.device.DeviceMessageSender;
import net.lab1024.sa.base.device.send.FunctionInvokeSendOperation;
import net.lab1024.sa.base.device.send.ReadPropertySendOperation;
import net.lab1024.sa.base.device.send.WritePropertySendOperation;
import reactor.core.publisher.Mono;

/**
 * 设备下发操作者默认实现 — 持有 DeviceMessageSender（本地/集群按配置装配）+ DeviceRegistry（操作对象查物模型），
 * readProperty() 等返回操作对象，操作对象的 send() 回调本类的 send()。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public class DefaultDeviceSendOperator implements DeviceSendOperator {

    private final DeviceMessageSender messageSender;
    private final DeviceRegistry registry;

    public DefaultDeviceSendOperator(DeviceMessageSender messageSender, DeviceRegistry registry) {
        this.messageSender = messageSender;
        this.registry = registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends DeviceMessage> Mono<R> send(DeviceMessage message) {
        // 回复按具体类型 R 转换返回（deviceId 由消息携带，发送器内部从消息取）
        return messageSender.sendAndWait(message)
                .map(reply -> (R) reply);
    }

    @Override
    public ReadPropertySendOperation readProperty() {
        // 操作对象 send() 前需查物模型校验，传入 registry
        return new ReadPropertySendOperation(this, registry);
    }

    @Override
    public WritePropertySendOperation writeProperty() {
        return new WritePropertySendOperation(this, registry);
    }

    @Override
    public FunctionInvokeSendOperation invokeFunction() {
        // 功能调用需查物模型（校验 + isAsync 决定 async header），传入 registry
        return new FunctionInvokeSendOperation(this, registry);
    }
}
