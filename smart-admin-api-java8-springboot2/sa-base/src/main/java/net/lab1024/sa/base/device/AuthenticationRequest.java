package net.lab1024.sa.base.device;


import net.lab1024.sa.base.common.message.codec.Transport;

/**
 * 设备认证请求。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/07/22
 * &#064;Copyright  1024创新实验室
 */

public interface AuthenticationRequest {


    Transport getTransport();

    String getDeviceKey();

    String getProductKey();
}
