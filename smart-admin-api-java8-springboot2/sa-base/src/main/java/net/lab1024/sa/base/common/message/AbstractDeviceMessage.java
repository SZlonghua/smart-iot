package net.lab1024.sa.base.common.message;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * 设备消息抽象基类 — 提供 deviceId/messageId/timestamp/headers 默认实现。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Getter
@Setter
public abstract class AbstractDeviceMessage implements DeviceMessage {

    private String productKey;
    private String deviceKey;
    private String deviceId;
    private String messageId;
    private long timestamp = System.currentTimeMillis();
    private Map<String, Object> headers = new ConcurrentHashMap<String, Object>();

    @Override
    public DeviceMessage addHeader(String header, Object value) {
        headers.put(header, value);
        return this;
    }

    @Override
    public DeviceMessage addHeaderIfAbsent(String header, Object value) {
        headers.putIfAbsent(header, value);
        return this;
    }

    @Override
    public Object computeHeader(String key, BiFunction<String, Object, Object> computer) {
        return headers.compute(key, computer);
    }

    @Override
    public DeviceMessage removeHeader(String header) {
        if (headers != null) {
            headers.remove(header);
        }
        return this;
    }
}
