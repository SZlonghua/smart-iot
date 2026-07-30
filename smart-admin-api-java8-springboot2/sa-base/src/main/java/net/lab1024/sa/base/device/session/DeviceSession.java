package net.lab1024.sa.base.device.session;

import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.common.message.codec.Transport;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

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

    default Mono<Boolean> isAliveAsync() {
        return Mono.fromSupplier(this::isAlive);
    }

}
