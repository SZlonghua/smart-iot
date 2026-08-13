package net.lab1024.sa.base.device;

import lombok.*;

/**
 * 设备认证响应。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Getter
@Setter
@ToString
public class AuthenticationResponse {

    private boolean success;

    private int code;

    private String message;

    private String deviceId;

    public static AuthenticationResponse success() {
        return success(null);
    }


    public static AuthenticationResponse success(String deviceId) {
        AuthenticationResponse response = new AuthenticationResponse();
        response.success = true;
        response.code = 200;
        response.message = "授权通过";
        response.deviceId = deviceId;
        return response;
    }

    public static AuthenticationResponse error(int code, String message) {
        AuthenticationResponse response = new AuthenticationResponse();
        response.success = false;
        response.code = code;
        response.message = message;
        return response;
    }
}
