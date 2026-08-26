package net.lab1024.sa.base.common.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /**
     * 构造外层回复（网关代理）：
     * 外层 = 网关三元（gatewayDeviceId / gatewayDeviceKey / gatewayProductKey）；
     * 外层与内层是同一条消息 — 外层 messageId = 内层回复 messageId（回复按它绑定发送方等待），
     * 子设备三元（childProductKey / childDeviceKey）取内层回复 productKey / deviceKey。
     * 注：解码场景 gatewayDeviceId 未知传 null，由网关连接认证后回填。
     */
    @SuppressWarnings("unchecked")
    public ChildDeviceMessageReply(String gatewayDeviceId, String gatewayDeviceKey, String gatewayProductKey, T childDeviceMessage) {
        setDeviceId(gatewayDeviceId);
        setDeviceKey(gatewayDeviceKey);
        setProductKey(gatewayProductKey);
        this.childDeviceMessage = childDeviceMessage;
        if (childDeviceMessage instanceof AbstractDeviceMessage) {
            AbstractDeviceMessage inner = (AbstractDeviceMessage) childDeviceMessage;
            this.childProductKey = inner.getProductKey();
            this.childDeviceKey = inner.getDeviceKey();
            setMessageId(inner.getMessageId());
            setTimestamp(inner.getTimestamp());
        }
    }

    /** 仅用于事件总线泛型匹配 — 序列化消息 payload 时排除（ResolvableType 自引用会导致 Jackson 递归报错） */
    @Override
    @JsonIgnore
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                ChildDeviceMessageReply.class,
                ResolvableType.forClass(childDeviceMessage.getClass()));
    }
}
