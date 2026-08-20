package net.lab1024.sa.base.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * 子设备消息回复 — 网关回复平台下发给子设备的指令结果。
 * <p>
 * 实现 {@link ResolvableTypeProvider}，事件总线按泛型精准匹配。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChildDeviceMessageReply<T extends Message> extends AbstractDeviceMessageReply
        implements ResolvableTypeProvider {

    /** 子设备产品 Key（烧录标识） */
    private String childProductKey;

    /** 子设备 Key（烧录标识） */
    private String childDeviceKey;

    /** 子设备回复消息 */
    private T childDeviceMessage;

    public ChildDeviceMessageReply(String gatewayDeviceKey, String childProductKey, String childDeviceKey, T childDeviceMessage) {
        setDeviceKey(gatewayDeviceKey);
        this.childProductKey = childProductKey;
        this.childDeviceKey = childDeviceKey;
        this.childDeviceMessage = childDeviceMessage;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                ChildDeviceMessageReply.class,
                ResolvableType.forClass(childDeviceMessage.getClass()));
    }
}
