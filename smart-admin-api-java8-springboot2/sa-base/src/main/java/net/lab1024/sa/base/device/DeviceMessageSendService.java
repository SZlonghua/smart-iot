package net.lab1024.sa.base.device;

import net.lab1024.sa.base.common.message.DeviceMessage;
import net.lab1024.sa.base.common.message.DeviceMessageReply;
import reactor.core.publisher.Mono;

/**
 * 设备消息发送服务 — 集群远程节点代理接口。
 * 远程节点经 clusterManager.getService(nodeId, DeviceMessageSendService.class) 取代理调用，
 * 本节点实现转发 LocalDeviceMessageSender（见 ClusterDeviceMessageSender 构造注册）。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public interface DeviceMessageSendService {

    /** 在目标节点执行下发并等待回复（远程代理调用，目标节点本地等待设备回复，deviceId 取自消息） */
    Mono<DeviceMessageReply> sendAndWait(DeviceMessage message, long timeout);

    /** 在目标节点执行异步下发（不等待设备回复）— 本节点直接返回，回复不跨节点回传（调用方不需要） */
    Mono<Void> send(DeviceMessage message);
}
