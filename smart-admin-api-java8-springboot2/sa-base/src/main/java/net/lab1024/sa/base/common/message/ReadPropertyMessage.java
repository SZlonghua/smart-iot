package net.lab1024.sa.base.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 读取属性消息（下行）。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReadPropertyMessage extends AbstractDeviceMessage
        implements RepayableDeviceMessage<ReadPropertyMessageReply> {

    /** 要读取的属性 ID 列表 */
    private List<String> properties;

    @Override
    public ReadPropertyMessageReply newReply() {
        ReadPropertyMessageReply reply = new ReadPropertyMessageReply();
        reply.setDeviceKey(getDeviceKey());
        reply.setMessageId(getMessageId());
        return reply;
    }
}
