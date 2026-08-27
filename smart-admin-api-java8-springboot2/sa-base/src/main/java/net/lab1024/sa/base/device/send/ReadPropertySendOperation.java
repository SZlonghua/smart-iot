package net.lab1024.sa.base.device.send;

import cn.hutool.core.collection.CollUtil;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.message.ReadPropertyMessage;
import net.lab1024.sa.base.common.message.ReadPropertyMessageReply;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.DeviceSendOperator;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 读属性操作 — send() 先取物模型校验（属性存在性，校验在物模型内部，见第六章），
 * 再构造 ReadPropertyMessage 回调 DeviceSendOperator.send()。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public class ReadPropertySendOperation {

    private final DeviceSendOperator operator;
    private final DeviceRegistry registry;

    public ReadPropertySendOperation(DeviceSendOperator operator, DeviceRegistry registry) {
        this.operator = operator;
        this.registry = registry;
    }

    /** 下发读属性并等待回复（属性值见 reply.getProperties()） */
    public Mono<ReadPropertyMessageReply> send(String deviceId, List<String> properties) {
        return registry.getDevice(deviceId)                    // ① 取物模型校验（物模型内部校验）
                .flatMap(DeviceOperator::getMetadata)
                .doOnNext(metadata -> checkErrors(metadata.validateReadProperties(properties)))
                .then(operator.send(buildMessage(deviceId, properties)));   // ② 校验通过 → 构造消息下发
    }

    private ReadPropertyMessage buildMessage(String deviceId, List<String> properties) {
        ReadPropertyMessage message = new ReadPropertyMessage();
        message.setDeviceId(deviceId);
        message.setProperties(properties);
        return message;
    }

    /** 校验失败（错误列表非空）→ 抛 BusinessException */
    private void checkErrors(List<String> errors) {
        if (CollUtil.isNotEmpty(errors)) {
            throw new BusinessException(String.join("; ", errors));
        }
    }
}
