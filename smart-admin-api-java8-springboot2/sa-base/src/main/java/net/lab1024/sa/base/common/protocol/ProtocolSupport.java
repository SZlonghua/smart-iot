package net.lab1024.sa.base.common.protocol;

import net.lab1024.sa.base.common.defaults.Authenticator;
import net.lab1024.sa.base.common.message.codec.DeviceMessageCodec;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.device.AuthenticationResponse;
import org.springframework.core.Ordered;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.AuthenticationRequest;

import javax.annotation.Nonnull;

public interface ProtocolSupport extends Disposable, Ordered, Comparable<ProtocolSupport>{

    /**
     * @return 协议ID
     */
    @Nonnull
    String getId();

    /**
     * @return 协议名称
     */
    String getName();

    /**
     * @return 说明
     */
    String getDescription();

    /**
     * @return 获取支持的协议类型
     */
    Flux<? extends Transport> getSupportedTransport();

    /**
     * 获取设备消息编码解码器
     * <ul>
     * <li>用于将平台统一的消息对象转码为设备的消息</li>
     * <li>用于将设备发送的消息转吗为平台统一的消息对象</li>
     * </ul>
     *
     * @return 消息编解码器
     */
    @Nonnull
    Mono<? extends DeviceMessageCodec> getMessageCodec(Transport transport);


    void addAuthenticator(Transport transport, Authenticator authenticator);

    void addMessageCodec(Transport transport, DeviceMessageCodec codec);

    default void addMessageCodec(DeviceMessageCodec codec) {
        addMessageCodec(codec.getSupportTransport(), codec);
    }


    /**
     * 进行设备认证
     *
     * @param request         认证请求，不同的连接方式实现不同
     * @param deviceOperation 设备操作接口,可用于配置设备
     * @return 认证结果
//     * @see MqttAuthenticationRequest
     */
    @Nonnull
    Mono<AuthenticationResponse> authenticate(
            @Nonnull AuthenticationRequest request,
            @Nonnull DeviceOperator deviceOperation);

    /**
     * 销毁协议
     */
    @Override
    default void dispose() {
    }

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    default int compareTo(@Nonnull ProtocolSupport o) {
        return Integer.compare(this.getOrder(), o.getOrder());
    }
}
