package net.lab1024.sa.base.device.support;

import net.lab1024.sa.base.common.message.DeviceMessage;
import net.lab1024.sa.base.common.message.DeviceMessageReply;
import net.lab1024.sa.base.device.DeviceMessageSendService;
import reactor.core.publisher.Mono;

/**
 * 设备消息发送服务默认实现 — 转发本地发送器（设备所在节点执行时的方法调用），
 * 供 ClusterDeviceMessageSender 构造时注册为本节点集群服务。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public class DefaultDeviceMessageSendService implements DeviceMessageSendService {

    private final LocalDeviceMessageSender localSender;

    public DefaultDeviceMessageSendService(LocalDeviceMessageSender localSender) {
        this.localSender = localSender;
    }

    @Override
    public Mono<DeviceMessageReply> sendAndWait(DeviceMessage message, long timeout) {
        return localSender.sendAndWait(message, timeout);
    }

    @Override
    public Mono<Void> send(DeviceMessage message) {
        return localSender.send(message);
    }
}
