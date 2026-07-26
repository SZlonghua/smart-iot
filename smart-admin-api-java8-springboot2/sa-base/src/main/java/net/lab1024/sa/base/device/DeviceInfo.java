package net.lab1024.sa.base.device;

import lombok.Builder;
import lombok.Data;

/**
 * 设备注册信息 DTO。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Data
@Builder
public class DeviceInfo {

    private String deviceId;
    private String deviceName;
    private String deviceKey;
    private String deviceSecret;
    private String productId;
    private String productName;
    private String productKey;
    private String productSecret;
    private Long offlineTime;
    private Long onlineTime;
    private String sessionId;
    private String protocolId;
    private String gatewayId;
    private String connectionServerId;
}
