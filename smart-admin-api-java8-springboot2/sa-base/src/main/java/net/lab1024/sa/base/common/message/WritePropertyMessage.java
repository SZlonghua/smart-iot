package net.lab1024.sa.base.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 修改属性消息（下行）。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WritePropertyMessage extends AbstractDeviceMessage
        implements RepayableDeviceMessage<WritePropertyMessageReply> {

    /** 要修改的属性 ID → 属性值 */
    private Map<String, Object> properties;

    @Override
    public WritePropertyMessageReply newReply() {
        WritePropertyMessageReply reply = new WritePropertyMessageReply();
        reply.setDeviceKey(getDeviceKey());
        reply.setMessageId(getMessageId());
        return reply;
    }
}
