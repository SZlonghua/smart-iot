package net.lab1024.sa.base.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 断开设备连接消息（下行）。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DisconnectDeviceMessage extends AbstractDeviceMessage
        implements RepayableDeviceMessage<DisconnectDeviceMessageReply> {

    @Override
    public DisconnectDeviceMessageReply newReply() {
        DisconnectDeviceMessageReply reply = new DisconnectDeviceMessageReply();
        reply.setDeviceKey(getDeviceKey());
        reply.setMessageId(getMessageId());
        return reply;
    }
}
