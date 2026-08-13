package net.lab1024.sa.base.common.message.codec;

import net.lab1024.sa.base.common.Wrapper;
import net.lab1024.sa.base.device.DeviceOperator;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

public interface MessageCodecContext extends Wrapper {

    /**
     * 获取当前上下文中到设备操作接口,
     * 在tcp,http等场景下,此接口可能返回{@code null}
     *
     * @return DeviceOperator
     */
    @Nullable
    DeviceOperator getDevice();

    /**
     * 同{@link MessageCodecContext#getDevice()},只是返回结果是Mono,不会为null.
     *
     * @return Mono<DeviceOperator>
     * @since 1.1.2
     */
    default Mono<DeviceOperator> getDeviceAsync() {
        return Mono.justOrEmpty(getDevice());
    }

}
