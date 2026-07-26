package net.lab1024.sa.base.device;

import lombok.Builder;
import lombok.Data;

/**
 * 设备认证响应。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Data
@Builder
public class AuthenticationResponse {

    private boolean success;
    private String deviceId;
    private String message;
}
