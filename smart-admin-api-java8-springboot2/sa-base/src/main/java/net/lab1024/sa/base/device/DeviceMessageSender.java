package net.lab1024.sa.base.device;

import net.lab1024.sa.base.common.message.DeviceMessage;
import net.lab1024.sa.base.common.message.DeviceMessageReply;
import reactor.core.publisher.Mono;

/**
 * 设备消息发送服务接口 — 向设备下发命令并（可选）等待回复。
 * 实现：LocalDeviceMessageSender（单机）/ ClusterDeviceMessageSender（集群路由）。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public interface DeviceMessageSender {

    /** 默认下发超时（毫秒）— 消息缺省 timeout header 时，prepareMessage 发送前回填此值（10s 与 MQTT 协议约定一致） */
    long DEFAULT_TIMEOUT_MS = 10_000L;

    /** 同步下发并等待回复 — 超时以消息 timeout header 为准（缺省由 prepareMessage 回填默认 10s，不允许未设置），deviceId 取自消息 */
    default Mono<DeviceMessageReply> sendAndWait(DeviceMessage message) {
        return sendAndWait(message, DEFAULT_TIMEOUT_MS);
    }

    /** 同步下发并等待回复 — 自定义超时（毫秒）：正值时写入消息 timeout header，按 header 等待（调用方已设置的 header 优先，最终以 header 为准） */
    Mono<DeviceMessageReply> sendAndWait(DeviceMessage message, long timeout);

    /** 异步下发（async=true）— 不等待回复，下发即成功，无需超时 */
    Mono<Void> send(DeviceMessage message);
}
