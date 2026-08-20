package net.lab1024.sa.base.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 设备注册消息（上行）— productId/deviceName 存 header。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeviceRegisterMessage extends AbstractDeviceMessage {
}
