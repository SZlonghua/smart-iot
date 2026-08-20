package net.lab1024.sa.base.device.session;

import net.lab1024.sa.base.common.message.raw.EncodedMessage;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.common.message.codec.Transport;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface DeviceSession {

    /**
     * @return 会话ID
     */
    String getId();

    /**
     * @return 设备ID
     */
    String getDeviceId();

    /**
     * 获取设备操作对象,在类似TCP首次请求的场景下,返回值可能为<code>null</code>.
     * 可以通过判断此返回值是否为<code>null</code>,来处理首次连接的情况。
     *
     * @return void
     */
    @Nullable
    DeviceOperator getOperator();

    /**
     * @return 最近心跳时间
     */
    long lastPingTime();

    /**
     * @return 创建时间
     */
    long connectTime();

    /**
     * 发送消息
     * 预留
     * @param encodedMessage 消息
     * @return 是否成功
     */
//    Mono<Boolean> send(EncodedMessage encodedMessage);

    /**
     * 传输协议,比如MQTT,TCP等
     *
     * @return void
     */
    Transport getTransport();


    /**
     * 关闭session
     */
    void close();

    /**
     * @return 会话是否存活
     */
    boolean isAlive();

    /**
     * @return 网关实例ID（设备接入的 DeviceGateway.getId()，如 "mqtt-server"），无网关返回 null
     */
    @Nullable
    default String getGatewayId() {
        return "";
    }

    default Mono<Boolean> isAliveAsync() {
        return Mono.fromSupplier(this::isAlive);
    }

    /**
     * 注册会话关闭监听器 — 会话关闭时回调（如子设备会话监听父会话关闭做级联清理）
     */
    default void onClose(Consumer<DeviceSession> listener) {
    }

    /**
     * 移除会话关闭监听器 — 与 {@link #onClose} 对应，监听方 close() 时注销自己，
     * 防止会话存活期间长期持有监听方引用导致内存泄漏
     */
    default void removeOnClose(Consumer<DeviceSession> listener) {
    }

}
