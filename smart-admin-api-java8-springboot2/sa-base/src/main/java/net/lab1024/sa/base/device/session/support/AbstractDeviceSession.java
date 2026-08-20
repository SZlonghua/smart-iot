package net.lab1024.sa.base.device.session.support;

import lombok.Setter;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.common.message.codec.Transport;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 设备会话抽象基类 — 提供公共字段和默认实现。
 * 连接型会话持有 Channel/Connection，无连接型仅追踪时间戳。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/07/27
 * &#064;Copyright  1024创新实验室
 */
public abstract class AbstractDeviceSession implements DeviceSession {

    protected final String id;
    protected final String deviceId;
    protected final Transport transport;
    protected final long connectTime;

    /** 产品 Key（烧录标识，可为空 — 会话注册时建立 key 索引） */
    @Nullable
    protected final String productKey;

    /** 设备 Key（烧录标识，可为空） */
    @Nullable
    protected final String deviceKey;

    /** 网关实例 ID（设备接入的 DeviceGateway.getId()，可为空） */
    @Nullable
    protected final String gatewayId;

    @Setter
    @Nullable
    protected DeviceOperator operator;

    /** 会话关闭监听器链 — 子会话等监听本会话关闭做级联清理 */
    private final List<Consumer<DeviceSession>> closeListeners = new CopyOnWriteArrayList<>();

    protected AbstractDeviceSession(String deviceId, @Nullable DeviceOperator deviceOperator, Transport transport,
                                    @Nullable String productKey, @Nullable String deviceKey,
                                    @Nullable String gatewayId) {
        this.id = deviceId;
        this.deviceId = deviceId;
        this.transport = transport;
        this.operator = deviceOperator;
        this.productKey = productKey;
        this.deviceKey = deviceKey;
        this.gatewayId = gatewayId;
        this.connectTime = System.currentTimeMillis();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    @Nullable
    public DeviceOperator getOperator() {
        return operator;
    }

    @Override
    public long connectTime() {
        return connectTime;
    }

    @Override
    public Transport getTransport() {
        return transport;
    }

    @Nullable
    public String getProductKey() {
        return productKey;
    }

    @Nullable
    public String getDeviceKey() {
        return deviceKey;
    }

    @Override
    @Nullable
    public String getGatewayId() {
        return gatewayId;
    }

    @Override
    public void onClose(Consumer<DeviceSession> listener) {
        closeListeners.add(listener);
    }

    @Override
    public void removeOnClose(Consumer<DeviceSession> listener) {
        closeListeners.remove(listener);
    }

    /** 触发关闭监听器 — 子类 close() 实现中调用 */
    protected void notifyClose() {
        for (Consumer<DeviceSession> listener : closeListeners) {
            listener.accept(this);
        }
    }

}
