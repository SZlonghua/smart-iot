package net.lab1024.sa.base.module.support.protocol.coap.session;

import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;
import net.lab1024.sa.base.message.codec.DefaultTransport;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CoAP 设备会话 — 无连接型，仅追踪设备存在性和 Observe 关系。
 * 不持有 Socket/Channel，无法实时下发消息。
 * 下行消息通过"设备下次请求时捎带"机制投递。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
public class CoapDeviceSession extends AbstractDeviceSession {

    /** 超时时间：120 秒无请求视为离线 */
    private static final long TIMEOUT_MS = 120_000;

    /** Observe 订阅的资源路径集合 */
    private final Set<String> observeResources = ConcurrentHashMap.newKeySet();

    public CoapDeviceSession(String deviceId) {
        super(deviceId, DefaultTransport.CoAP);
    }

    @Override
    public boolean isAlive() {
        return !closed && (System.currentTimeMillis() - lastPingTime) < TIMEOUT_MS;
    }

    @Override
    public void close() {
        super.close();
        observeResources.clear();
    }

    /** 添加 Observe 订阅 */
    public void addObserve(String resourcePath) {
        observeResources.add(resourcePath);
    }

    /** 移除 Observe 订阅 */
    public void removeObserve(String resourcePath) {
        observeResources.remove(resourcePath);
    }

    /** 是否订阅了指定资源 */
    public boolean isObserving(String resourcePath) {
        return observeResources.contains(resourcePath);
    }
}
