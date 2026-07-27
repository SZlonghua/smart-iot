package net.lab1024.sa.base.device.session;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Predicate;

public interface DeviceSessionManager {


    /*Mono<DeviceSession> computeIfAbsent(@Nonnull String deviceId,
                                @Nonnull Function<String, Mono<DeviceSession>> creator);*/

    Mono<DeviceSession> compute(@Nonnull String deviceId,
                               @Nonnull Function<Mono<DeviceSession>, Mono<DeviceSession>> computer);

    Mono<DeviceSession> getSession(String deviceId);

    /**
     * 获取设备会话.会话不存在则返回{@link Mono#empty()}.
     *
     * @param deviceId               设备ID
     * @param unregisterWhenNotAlive 当会话失效时,是否注销会话
     * @return 会话信息
     */
    Mono<DeviceSession> getSession(String deviceId, boolean unregisterWhenNotAlive);


    Flux<DeviceSession> getSessions();

    /**
     * 移除会话,如果会话存在将触发DeviceSessionEvent
     * @param deviceId
     * @return
     */
    Mono<Long> remove(String deviceId);

    /**
     * 根据自定义判断逻辑来移除当前服务节点的会话
     *
     * @param deviceId  设备ID
     * @param predicate 判断逻辑
     * @return 有多少会话被移除 0 or 1
     * @since 1.2.3
     */
    Mono<Long> remove(String deviceId, Predicate<DeviceSession> predicate);


    Mono<Boolean> isAlive(String deviceId);

    Mono<Long> totalSessions();

    /**
     * 监听并处理会话事件,可通过调用返回值{@link  Disposable#dispose()}来取消监听
     *
     * @param handler 事件处理器
     * @return Disposable
     */
    Disposable listenEvent(Function<DeviceSessionEvent, Mono<Void>> handler);
}
