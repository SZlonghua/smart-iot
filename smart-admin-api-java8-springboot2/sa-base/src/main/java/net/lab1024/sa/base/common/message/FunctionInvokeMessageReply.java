package net.lab1024.sa.base.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 功能调用回复消息（上行）。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FunctionInvokeMessageReply extends AbstractDeviceMessageReply {

    /** 功能输出结果，类型与物模型功能输出类型一致 */
    private Object output;
}
