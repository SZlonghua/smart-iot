package net.lab1024.sa.base.common.message.codec;

import net.lab1024.sa.base.common.message.raw.EncodedMessage;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.ClientConnection;
import net.lab1024.sa.base.device.session.DeviceSession;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface FromDeviceMessageContext extends MessageDecodeContext {

    /**
     * 获取设备会话
     *
     * @return 设备会话
     */
    DeviceSession getSession();

    /**
     * 获取连接信息,在1.2.3版本后,一个设备可能存在多个连接,此方法用于获取当前连接信息.
     * <p>
     * 如果返回null,说明当前接入方式不支持多连接,或者无法获取连接信息.
     *
     * @return 连接信息
     * @since 1.2.3
     */
    default ClientConnection getConnection() {
        return null;
    }

    /**
     * @see MessageDecodeContext#getDevice()
     */
    @Override
    @Nullable
    default DeviceOperator getDevice() {
        return getSession().getOperator();
    }


    static FromDeviceMessageContext of(DeviceSession session,
                                       EncodedMessage message,
                                       DeviceRegistry registry,
                                       ClientConnection connection) {
        return new FromDeviceMessageContext() {
            @Override
            public ClientConnection getConnection() {
                return connection;
            }

            @Override
            public DeviceSession getSession() {
                return session;
            }

            @Nonnull
            @Override
            public EncodedMessage getMessage() {
                return message;
            }

            @Override
            public Mono<DeviceOperator> getDevice(String deviceId) {
                return registry.getDevice(deviceId);
            }

            @Override
            public Mono<DeviceOperator> getDevice(String productKey, String deviceKey) {
                return registry.getDevice(productKey, deviceKey);
            }

        };
    }
}
