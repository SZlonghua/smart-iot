package net.lab1024.sa.base.device.session.support;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.DeviceSessionEvent;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import org.apache.commons.lang3.StringUtils;
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
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/07/27
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
public class LocalDeviceSessionManager implements DeviceSessionManager {

    private final Map<String, DeviceSession> sessions = new ConcurrentHashMap<>();
    private final List<Function<DeviceSessionEvent, Mono<Void>>> listeners = new CopyOnWriteArrayList<>();
    /** 子设备烧录标识索引 — productKey:deviceKey → deviceId，避免每条消息查 registry */
    private final Map<String, String> sessionKeyIndex = new ConcurrentHashMap<>();

    @Override
    public Mono<DeviceSession> compute(@Nonnull String deviceId,
                                       @Nonnull Function<Mono<DeviceSession>, Mono<DeviceSession>> computer) {
        return Mono.justOrEmpty(sessions.get(deviceId))
                .transform(computer)
                .doOnNext(session -> {
                    DeviceSession old = sessions.put(deviceId, session);
                    if (old != null && old != session) {
                        // 新会话替换旧会话 — 内部关闭旧会话并移除其索引，不扩散到调用方
                        removeIndexSession(old);
                        old.close();
                    }
                    indexSession(session);
                    if (old == null) {
                        fireEvent(DeviceSessionEvent.of(DeviceSessionEvent.Type.register, session));
                    }
                });
    }

    @Override
    public Mono<DeviceSession> getSession(String deviceId) {
        return getSession(deviceId,false);
    }

    @Override
    public Mono<DeviceSession> getSession(String productKey, String deviceKey) {
        String deviceId = sessionKeyIndex.get(buildKey(productKey, deviceKey));
        return deviceId == null ? Mono.empty() : Mono.justOrEmpty(sessions.get(deviceId));
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
            removeIndexSession(removed);
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
            removeIndexSession(session);
            session.close();
            fireEvent(DeviceSessionEvent.of(DeviceSessionEvent.Type.unregister, session));
            return Mono.just(1L);
        }
        return Mono.just(0L);
    }

    private void indexSession(DeviceSession session) {
        if (session instanceof AbstractDeviceSession) {
            AbstractDeviceSession deviceSession = (AbstractDeviceSession) session;
            String productKey = deviceSession.getProductKey();
            String deviceKey = deviceSession.getDeviceKey();
            if (StringUtils.isNotEmpty(productKey) && StringUtils.isNotEmpty(deviceKey)) {
                sessionKeyIndex.put(buildKey(productKey, deviceKey), deviceSession.getDeviceId());
            }
        }
    }

    private void removeIndexSession(DeviceSession session) {
        if (session instanceof AbstractDeviceSession) {
            AbstractDeviceSession deviceSession = (AbstractDeviceSession) session;
            String productKey = deviceSession.getProductKey();
            String deviceKey = deviceSession.getDeviceKey();
            if (StringUtils.isNotEmpty(productKey) && StringUtils.isNotEmpty(deviceKey)) {
                sessionKeyIndex.remove(buildKey(productKey, deviceKey));
            }
        }
    }

    private String buildKey(String productKey, String deviceKey) {
        return productKey + ":" + deviceKey;
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
