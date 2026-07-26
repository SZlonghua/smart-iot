package net.lab1024.sa.base.device.support;

/**
 * Redis Hash 设备字段枚举。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
public enum DeviceField {

    DEVICE_NAME("deviceName"),
    DEVICE_KEY("deviceKey"),
    DEVICE_SECRET("deviceSecret"),
    PRODUCT_ID("productId"),
    PRODUCT_NAME("productName"),
    PRODUCT_KEY("productKey"),
    PRODUCT_SECRET("productSecret"),
    OFFLINE_TIME("offlineTime"),
    ONLINE_TIME("onlineTime"),
    SESSION_ID("sessionId"),
    PROTOCOL_ID("protocolId"),
    GATEWAY_ID("gatewayId"),
    CONNECTION_SERVER_ID("connectionServerId");

    private final String value;

    DeviceField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
