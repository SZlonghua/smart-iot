package net.lab1024.sa.base.device;

import lombok.Data;

/**
 * 设备认证请求。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Data
public class AuthenticationRequest {

    private String deviceId;
    private String deviceKey;
    private String deviceSecret;
}
