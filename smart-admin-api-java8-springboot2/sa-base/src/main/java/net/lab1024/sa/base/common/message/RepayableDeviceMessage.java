package net.lab1024.sa.base.common.message;

/**
 * 可回复设备消息 — 下行设备消息实现此接口。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
public interface RepayableDeviceMessage<R extends DeviceMessageReply> extends DeviceMessage, RepayableMessage<R> {
}
