package net.lab1024.sa.base.common.message;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * 设备消息回复抽象基类 — 提供 success/code/message 默认实现。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Getter
@Setter
public abstract class AbstractDeviceMessageReply extends AbstractDeviceMessage implements DeviceMessageReply {

    private boolean success;
    private String code;
    private String message;

    @Override
    @Nullable
    public String getCode() {
        return code;
    }

    @Override
    @Nullable
    public String getMessage() {
        return message;
    }
}
