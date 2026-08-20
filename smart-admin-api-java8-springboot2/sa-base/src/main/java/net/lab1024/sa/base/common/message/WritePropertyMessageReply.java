package net.lab1024.sa.base.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 修改属性回复消息（上行）。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WritePropertyMessageReply extends AbstractDeviceMessageReply {

    /** 修改后的最新属性值 */
    private Map<String, Object> properties;
}
