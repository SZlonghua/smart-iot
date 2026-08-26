package net.lab1024.sa.base.device.send;

import cn.hutool.core.collection.CollUtil;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.message.WritePropertyMessage;
import net.lab1024.sa.base.common.message.WritePropertyMessageReply;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.DeviceSendOperator;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 写属性操作 — send() 先取物模型校验（属性存在 + 非只读 + 值合法，校验在物模型内部，见第六章），
 * 再构造 WritePropertyMessage 回调 DeviceSendOperator.send()。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public class WritePropertySendOperation {

    private final DeviceSendOperator operator;
    private final DeviceRegistry registry;

    public WritePropertySendOperation(DeviceSendOperator operator, DeviceRegistry registry) {
        this.operator = operator;
        this.registry = registry;
    }

    /** 下发写属性并等待回复 */
    public Mono<WritePropertyMessageReply> send(String deviceId, Map<String, Object> properties) {
        return registry.getDevice(deviceId)                    // ① 取物模型校验（物模型内部校验）
                .flatMap(DeviceOperator::getMetadata)
                .doOnNext(metadata -> checkErrors(metadata.validateProperties(properties)))
                .then(operator.send(buildMessage(deviceId, properties)));   // ② 校验通过 → 构造消息下发
    }

    private WritePropertyMessage buildMessage(String deviceId, Map<String, Object> properties) {
        WritePropertyMessage message = new WritePropertyMessage();
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
