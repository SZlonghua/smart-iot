package net.lab1024.sa.base.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * 子设备消息 — 网关代理子设备上报。
 * <p>
 * 实现 {@link ResolvableTypeProvider}，事件总线按泛型精准匹配：
 * {@code new EventHandler<ChildDeviceMessage<EventMessage>>()}
 *
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChildDeviceMessage<T extends Message> extends AbstractDeviceMessage
        implements ResolvableTypeProvider {

    /** 子设备产品 Key（烧录标识） */
    private String childProductKey;

    /** 子设备 Key（烧录标识） */
    private String childDeviceKey;

    /** 子设备消息 */
    private T childDeviceMessage;

    public ChildDeviceMessage(String gatewayDeviceKey, String childProductKey, String childDeviceKey, T childDeviceMessage) {
        setDeviceKey(gatewayDeviceKey);
        this.childProductKey = childProductKey;
        this.childDeviceKey = childDeviceKey;
        this.childDeviceMessage = childDeviceMessage;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                ChildDeviceMessage.class,
                ResolvableType.forClass(childDeviceMessage.getClass()));
    }
}
