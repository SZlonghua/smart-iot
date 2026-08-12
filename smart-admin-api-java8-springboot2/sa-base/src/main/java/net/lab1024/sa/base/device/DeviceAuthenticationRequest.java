package net.lab1024.sa.base.device;

import lombok.Builder;
import lombok.Data;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.signature.SignatureManager;

/**
 * 设备认证请求 — 实现 AuthenticationRequest，提供签名计算和比对。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/11
 * &#064;Copyright  1024创新实验室
 */
@Data
@Builder
public class DeviceAuthenticationRequest implements AuthenticationRequest {

    private Transport transport;
    private String productKey;
    private String deviceKey;
    private long timestamp;
    private int mode;             // 1=一机一密, 2=一型一密
    private String signType;      // 签名算法标识，如 "sha256"
    private String signature;     // 签名值

    @Override
    public Transport getTransport() {
        return transport;
    }

    @Override
    public String getDeviceKey() {
        return deviceKey;
    }

    @Override
    public String getProductKey() {
        return productKey;
    }

    /** 用服务端密钥构建新的请求（提前计算签名），用于签名比对 */
    public DeviceAuthenticationRequest copy(String deviceSecret, String productSecret) {
        String secret = (mode == 1) ? deviceSecret : productSecret;
        String data = productKey + ":" + deviceKey + ":" + timestamp;
        return DeviceAuthenticationRequest.builder()
                .transport(transport)
                .productKey(productKey)
                .deviceKey(deviceKey)
                .timestamp(timestamp)
                .mode(mode)
                .signType(signType)
                .signature(SignatureManager.sign(signType, data, secret))
                .build();
    }

    /** 比较签名值是否一致 */
    public boolean isSignatureMatch(DeviceAuthenticationRequest other) {
        return this.signature != null && this.signature.equals(other.signature);
    }
}
