package net.lab1024.sa.base.device.support;

/**
 * Redis Hash 产品字段枚举。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
public enum ProductField {

    PRODUCT_NAME("productName"),
    METADATA("metadata"),
    PRODUCT_KEY("productKey"),
    PRODUCT_SECRET("productSecret"),
    IS_GATEWAY_DEVICE("isGatewayDevice");

    private final String value;

    ProductField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
