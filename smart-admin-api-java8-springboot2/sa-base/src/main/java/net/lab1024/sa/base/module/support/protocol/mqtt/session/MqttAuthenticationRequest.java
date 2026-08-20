package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import lombok.Builder;
import lombok.Data;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.device.AuthenticationRequest;
import net.lab1024.sa.base.device.DeviceAuthenticationRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * MQTT 认证请求
 * @author 廖涛
 */
@Data
@Builder
public class MqttAuthenticationRequest implements AuthenticationRequest {

    private String clientId;
    // {productKey}:{deviceKey}
    private String username;
    // 密码为签名
    private String password;
    private Transport transport;

    @Override
    public String getDeviceKey() {
        String[] parts = getAuthInfo();
        return parts[1];
    }

    @Override
    public String getProductKey() {
        String[] parts = getAuthInfo();
        return parts[0];
    }

    /**
     * 解析 MQTT CONNECT 报文为 DeviceAuthenticationRequest
     * clientId = "productKey:deviceKey:timestamp:mode:signType"
     * username = "productKey:deviceKey"（冒号连接，productKey 含 "-" 不能用 "-" 分隔）
     * password = signature(hex)
     */
    public DeviceAuthenticationRequest toDeviceRequest() {
        if (StringUtils.isAnyEmpty(clientId, username, password)) {
            throw new IllegalArgumentException("clientId/username/password 不能为空");
        }

        String[] parts = getAuthInfo();

        return DeviceAuthenticationRequest.builder()
                .transport(transport)
                .productKey(parts[0])
                .deviceKey(parts[1])
                .timestamp(Long.parseLong(parts[2]))
                .mode(Integer.parseInt(parts[3]))
                .signType(parts[4])
                .signature(password)
                .build();
    }

    private String[] getAuthInfo() {
        String[] parts = clientId.split(":");
        if (parts.length != 5) {
            throw new IllegalArgumentException("clientId 格式错误，期望 productKey:deviceKey:timestamp:mode:signType");
        }

        String[] userParts = username.split(":");
        if (userParts.length != 2) {
            throw new IllegalArgumentException("username 格式错误，期望 productKey:deviceKey");
        }

        if (!parts[0].equals(userParts[0]) || !parts[1].equals(userParts[1])) {
            throw new IllegalArgumentException("clientId 与 username 中 productKey/deviceKey 不一致");
        }
        return parts;
    }
}
