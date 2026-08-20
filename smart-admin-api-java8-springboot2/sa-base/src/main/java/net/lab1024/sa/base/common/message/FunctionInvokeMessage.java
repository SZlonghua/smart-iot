package net.lab1024.sa.base.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 调用设备功能消息（下行）。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FunctionInvokeMessage extends AbstractDeviceMessage
        implements RepayableDeviceMessage<FunctionInvokeMessageReply> {

    /** 功能标识 */
    private String functionId;

    /** 功能参数列表 */
    private List<FunctionParameter> inputs;

    @Override
    public FunctionInvokeMessageReply newReply() {
        FunctionInvokeMessageReply reply = new FunctionInvokeMessageReply();
        reply.setDeviceKey(getDeviceKey());
        reply.setMessageId(getMessageId());
        return reply;
    }
}
