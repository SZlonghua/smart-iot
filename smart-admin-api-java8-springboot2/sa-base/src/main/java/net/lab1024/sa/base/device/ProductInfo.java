package net.lab1024.sa.base.device;

import lombok.Builder;
import lombok.Data;

/**
 * 产品注册信息 DTO。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Data
@Builder
public class ProductInfo {

    private String productId;
    private String productName;
    private String metadata;
    private String productKey;
    private String productSecret;
    private Boolean isGatewayDevice;
}
