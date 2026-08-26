package net.lab1024.sa.base.common.message.codec;

import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.DeviceSession;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 下发编码上下文默认实现 — 携带目标会话与待下发消息，reply() 走接口默认空实现（下行无需回写）。
 *
 * @Author 廖涛
 * @Date 2026/08/25
 * @Copyright 1024创新实验室
 */
public class DefaultMessageEncodeContext implements MessageEncodeContext {

    private final DeviceSession session;
    private final Message message;

    private DefaultMessageEncodeContext(DeviceSession session, Message message) {
        this.session = session;
        this.message = message;
    }

    public static DefaultMessageEncodeContext of(DeviceSession session, Message message) {
        return new DefaultMessageEncodeContext(session, message);
    }

    @Override
    @Nullable
    public DeviceOperator getDevice() {
        return session.getOperator();
    }

    @Override
    public Mono<DeviceOperator> getDevice(String deviceId) {
        return Mono.justOrEmpty(session.getOperator());
    }

    @Override
    public Mono<DeviceOperator> getDevice(String productKey, String deviceKey) {
        return Mono.justOrEmpty(session.getOperator());
    }

    @Nonnull
    @Override
    public Message getMessage() {
        return message;
    }
}
