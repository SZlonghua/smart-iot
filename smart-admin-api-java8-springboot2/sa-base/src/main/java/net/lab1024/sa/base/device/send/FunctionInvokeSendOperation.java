package net.lab1024.sa.base.device.send;

import cn.hutool.core.collection.CollUtil;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.message.FunctionInvokeMessage;
import net.lab1024.sa.base.common.message.FunctionInvokeMessageReply;
import net.lab1024.sa.base.common.message.FunctionParameter;
import net.lab1024.sa.base.common.message.Headers;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.DeviceSendOperator;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 功能调用操作 — send() 先取物模型校验（功能存在 + 参数名 + 必填 + 值合法，校验在物模型内部，见第六章），
 * 构造 FunctionInvokeMessage 后回调 DeviceSendOperator.send()；物模型功能 isAsync=true 时自动补充 async header（下发即成功，不等回复）。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public class FunctionInvokeSendOperation {

    private final DeviceSendOperator operator;
    private final DeviceRegistry registry;

    public FunctionInvokeSendOperation(DeviceSendOperator operator, DeviceRegistry registry) {
        this.operator = operator;
        this.registry = registry;
    }

    /** 下发功能调用并等待回复（异步功能自动补充 async header，下发即成功） */
    public Mono<FunctionInvokeMessageReply> send(String deviceId, String functionId, Map<String, Object> params) {
        return registry.getDevice(deviceId)
                .flatMap(DeviceOperator::getMetadata)
                .doOnNext(metadata -> checkErrors(metadata.validateFunction(functionId, params)))   // ① 校验（物模型内部）
                .flatMap(metadata -> {
                    FunctionInvokeMessage message = buildMessage(deviceId, functionId, params);
                    // ② 物模型功能 isAsync=true → 补充 async header
                    if (metadata.getFunctionOrNull(functionId).isAsync()) {
                        message.addHeader(Headers.ASYNC.getValue(), true);
                    }
                    return operator.send(message);
                });
    }

    private FunctionInvokeMessage buildMessage(String deviceId, String functionId, Map<String, Object> params) {
        FunctionInvokeMessage message = new FunctionInvokeMessage();
        message.setDeviceId(deviceId);
        message.setFunctionId(functionId);
        message.setInputs(buildInputs(params));        // inputs 逐参数包装 FunctionParameter
        return message;
    }

    /** params 逐参数包装 FunctionParameter（保持参数顺序） */
    private List<FunctionParameter> buildInputs(Map<String, Object> params) {
        List<FunctionParameter> inputs = new ArrayList<FunctionParameter>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            inputs.add(new FunctionParameter(entry.getKey(), entry.getValue()));
        }
        return inputs;
    }

    /** 校验失败（错误列表非空）→ 抛 BusinessException */
    private void checkErrors(List<String> errors) {
        if (CollUtil.isNotEmpty(errors)) {
            throw new BusinessException(String.join("; ", errors));
        }
    }
}
