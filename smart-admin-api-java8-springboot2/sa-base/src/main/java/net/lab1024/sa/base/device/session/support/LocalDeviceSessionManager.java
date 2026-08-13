package net.lab1024.sa.base.device.session.support;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.DeviceSessionEvent;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * DeviceSessionManager 本地实现 — 基于 ConcurrentHashMap 管理设备会话。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
@Slf4j
public class LocalDeviceSessionManager implements DeviceSessionManager {

    private final Map<String, DeviceSession> sessions = new ConcurrentHashMap<>();
    private final List<Function<DeviceSessionEvent, Mono<Void>>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public Mono<DeviceSession> compute(@Nonnull String deviceId,
                                       @Nonnull Function<Mono<DeviceSession>, Mono<DeviceSession>> computer) {
        return Mono.justOrEmpty(sessions.get(deviceId))
                .transform(computer)
                .doOnNext(session -> {
                    boolean existed = sessions.containsKey(deviceId);
                    sessions.put(deviceId, session);
                    if (!existed) {
                        fireEvent(DeviceSessionEvent.of(DeviceSessionEvent.Type.register, session));
                    }
                });
    }

    @Override
    public Mono<DeviceSession> getSession(String deviceId) {
        return Mono.justOrEmpty(sessions.get(deviceId));
    }

    @Override
    public Mono<DeviceSession> getSession(String deviceId, boolean unregisterWhenNotAlive) {
        DeviceSession session = sessions.get(deviceId);
        if (session == null) {
            return Mono.empty();
        }
        if (!session.isAlive() && unregisterWhenNotAlive) {
            return remove(deviceId).then(Mono.empty());
        }
        return Mono.just(session);
    }

    @Override
    public Flux<DeviceSession> getSessions() {
        return Flux.fromIterable(sessions.values());
    }

    @Override
    public Mono<Long> remove(String deviceId) {
        DeviceSession removed = sessions.remove(deviceId);
        if (removed != null) {
            removed.close();
            fireEvent(DeviceSessionEvent.of(DeviceSessionEvent.Type.unregister, removed));
            return Mono.just(1L);
        }
        return Mono.just(0L);
    }

    @Override
    public Mono<Long> remove(String deviceId, Predicate<DeviceSession> predicate) {
        DeviceSession session = sessions.get(deviceId);
        if (session != null && predicate.test(session)) {
            sessions.remove(deviceId, session);
            session.close();
            fireEvent(DeviceSessionEvent.of(DeviceSessionEvent.Type.unregister, session));
            return Mono.just(1L);
        }
        return Mono.just(0L);
    }

    @Override
    public Mono<Boolean> isAlive(String deviceId) {
        DeviceSession session = sessions.get(deviceId);
        return Mono.just(session != null && session.isAlive());
    }

    @Override
    public Mono<Long> totalSessions() {
        return Mono.just((long) sessions.size());
    }

    @Override
    public Disposable listenEvent(Function<DeviceSessionEvent, Mono<Void>> handler) {
        listeners.add(handler);
        return () -> listeners.remove(handler);
    }

    private void fireEvent(DeviceSessionEvent event) {
        for (Function<DeviceSessionEvent, Mono<Void>> handler : listeners) {
            try {
                handler.apply(event).subscribe();
            } catch (Exception e) {
                log.error("[SessionManager] 事件处理异常 deviceId={} type={}", event.getSession().getDeviceId(), event.getType(), e);
            }
        }
    }
}
