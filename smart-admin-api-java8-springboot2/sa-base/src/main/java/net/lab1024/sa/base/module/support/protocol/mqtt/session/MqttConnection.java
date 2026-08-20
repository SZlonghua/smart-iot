package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import net.lab1024.sa.base.device.session.ClientConnection;
import net.lab1024.sa.base.module.support.protocol.mqtt.message.MqttEncodedMessage;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

public interface MqttConnection extends ClientConnection {

    String getClientId();

    String getDeviceId();

    /**
     * 产品 Key（烧录标识，认证成功后回填）
     */
    String getProductKey();

    /**
     * 设备 Key（烧录标识，认证成功后回填）
     */
    String getDeviceKey();

    void reject(MqttConnectReturnCode code);

    MqttConnection accept();

    Optional<MqttEncodedMessage> getWillMessage();

    /**
     * 获取MQTT认证信息
     *
     * @return 可选的MQTT认证信息
     */
    Optional<MqttAuth> getAuth();

    void close();

    /**
     * 监听断开连接
     *
     * @param listener 监听器
     */
    void onClose(Consumer<MqttConnection> listener);

    void keepAlive();

    Duration getKeepAliveTimeout();

    void setKeepAliveTimeout(Duration duration);

    long getLastPingTime();
}
