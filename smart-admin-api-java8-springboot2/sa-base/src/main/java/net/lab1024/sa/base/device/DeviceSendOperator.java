package net.lab1024.sa.base.device;

import net.lab1024.sa.base.common.message.DeviceMessage;
import net.lab1024.sa.base.device.send.FunctionInvokeSendOperation;
import net.lab1024.sa.base.device.send.ReadPropertySendOperation;
import net.lab1024.sa.base.device.send.WritePropertySendOperation;
import reactor.core.publisher.Mono;

/**
 * 设备下发操作者 — 业务层下发门面，屏蔽底层发送器（本地/集群）。
 * 四个方法：send（通用下发，等待回复）、readProperty / writeProperty / invokeFunction（返回操作对象）。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public interface DeviceSendOperator {

    /** 通用下发并等待回复 — deviceId 取自消息，默认 10s 超时，返回具体回复类型（调用方按 R 取用） */
    <R extends DeviceMessage> Mono<R> send(DeviceMessage message);

    /** 读属性操作对象 */
    ReadPropertySendOperation readProperty();

    /** 写属性操作对象 */
    WritePropertySendOperation writeProperty();

    /** 功能调用操作对象 */
    FunctionInvokeSendOperation invokeFunction();
}
