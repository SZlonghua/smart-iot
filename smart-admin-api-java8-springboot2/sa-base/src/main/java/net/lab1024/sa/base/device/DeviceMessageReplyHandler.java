package net.lab1024.sa.base.device;

import net.lab1024.sa.base.common.message.AbstractDeviceMessageReply;

/**
 * 设备回复处理接口 — 设备回复消息到达时的回调（按 messageId 匹配发送时的 pending 等待；
 * 子设备回复 ChildDeviceMessageReply 在实现内解包后下发内层回复 — 见 3.5 onReply）。
 * 实现：LocalDeviceMessageSender（发送与回复处理内聚一体）。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public interface DeviceMessageReplyHandler {

    /** 处理设备回复消息 — 按 messageId 匹配并完成等待的调用方 */
    void onReply(AbstractDeviceMessageReply reply);
}
