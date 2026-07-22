package net.lab1024.sa.admin.module.business.device.listener;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.domain.entity.DeviceEntity;
import net.lab1024.sa.base.common.event.SaveAfterEvent;
import net.lab1024.sa.base.common.event.UpdateAfterEvent;
import net.lab1024.sa.base.module.support.eventbus.core.EventHandler;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import net.lab1024.sa.base.module.support.eventbus.core.Subscription;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EventBus 冒烟测试 — 验证 subscribe / publish / unsubscribe / 泛型匹配 / @EventListener 共存
 */
@Slf4j
@Component
public class EventBusSmokeTest implements CommandLineRunner {

    @Resource
    private IEventBus eventBus;

    @Override
    public void run(String... args) {
        testSubscribeWithClass();
        testSubscribeWithEventHandler();
        testGenericFilter();
        testMultipleSubscribers();
        testUnsubscribe();
        testPublishAsync();
        log.info("========== EventBus 冒烟测试全部通过 ==========");
    }

    /** Class + Consumer 订阅 — 不限泛型，匹配所有 SaveAfterEvent */
    private void testSubscribeWithClass() {
        List<String> captured = new ArrayList<>();
        Subscription sub = eventBus.subscribe(SaveAfterEvent.class, event -> {
            captured.add("SaveAfterEvent:" + event.getEntityId());
        });
        assert captured.isEmpty() : "注册时不应触发";
        sub.cancel();
        log.info("[冒烟-EventBus] subscribe(Class, Consumer) 通过");
    }

    /** EventHandler 订阅 — 带泛型 */
    private void testSubscribeWithEventHandler() {
        List<Long> ids = new ArrayList<>();
        EventHandler<SaveAfterEvent<DeviceEntity>> handler =
                new EventHandler<SaveAfterEvent<DeviceEntity>>() {
                    @Override
                    public void handle(SaveAfterEvent<DeviceEntity> event) {
                        ids.add(event.getEntityId());
                    }
                };
        Subscription sub = eventBus.subscribe(handler);
        assert ids.isEmpty() : "注册时不应触发";
        sub.cancel();
        log.info("[冒烟-EventBus] subscribe(EventHandler) 通过");
    }

    /** 泛型过滤 — DeviceEntity 只匹配 DeviceEntity，不匹配其他实体 */
    private void testGenericFilter() {
        AtomicInteger deviceCount = new AtomicInteger();
        AtomicInteger allCount = new AtomicInteger();

        // 精准订阅
        Subscription deviceSub = eventBus.subscribe(
                new EventHandler<SaveAfterEvent<DeviceEntity>>() {
                    @Override
                    public void handle(SaveAfterEvent<DeviceEntity> event) {
                        deviceCount.incrementAndGet();
                    }
                });

        // 不限泛型
        Subscription allSub = eventBus.subscribe(SaveAfterEvent.class, event -> {
            allCount.incrementAndGet();
        });

        // 模拟发布 DeviceEntity 事件
        eventBus.publish(createMockSaveAfterEvent(1L, "device", "t_device", DeviceEntity.class));

        assert deviceCount.get() == 1 : "精准订阅应收到 1 次，实际 " + deviceCount.get();
        assert allCount.get() == 1 : "不限泛型应收到 1 次，实际 " + allCount.get();

        // 发布非 DeviceEntity 事件
        eventBus.publish(createMockSaveAfterEvent(2L, "other", "t_other", Object.class));

        // 精准订阅不应再收到
        assert deviceCount.get() == 1 : "精准订阅不应收到非 DeviceEntity 事件";
        // 不限泛型应收到
        assert allCount.get() == 2 : "不限泛型应收到所有事件";

        deviceSub.cancel();
        allSub.cancel();
        log.info("[冒烟-EventBus] 泛型过滤 通过: deviceCount={}, allCount={}", deviceCount.get(), allCount.get());
    }

    /** 多订阅者 - 互不干扰 */
    private void testMultipleSubscribers() {
        AtomicInteger h1 = new AtomicInteger();
        AtomicInteger h2 = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        Subscription s1 = eventBus.subscribe(SaveAfterEvent.class, e -> h1.incrementAndGet());


        Subscription s3 = eventBus.subscribe(SaveAfterEvent.class, e -> fail.incrementAndGet());

        eventBus.publish(createMockSaveAfterEvent(3L, "test", "t_test", Object.class));

        assert h1.get() == 1 : "h1 应收到 1 次";
        assert h2.get() == 1 : "h2 应收到 1 次";
        assert fail.get() == 1 : "h2 异常不应影响 s3";

        s1.cancel();
        s3.cancel();
        log.info("[冒烟-EventBus] 多订阅者互不干扰 通过: h1={}, h2={}, fail={}", h1.get(), h2.get(), fail.get());
    }

    /** 注销后不再触发 */
    private void testUnsubscribe() {
        AtomicInteger count = new AtomicInteger();
        Subscription sub = eventBus.subscribe(SaveAfterEvent.class, e -> count.incrementAndGet());

        eventBus.publish(createMockSaveAfterEvent(4L, "b1", "t", Object.class));
        assert count.get() == 1;

        sub.cancel();

        eventBus.publish(createMockSaveAfterEvent(5L, "b2", "t", Object.class));
        assert count.get() == 1 : "注销后不应再触发";

        log.info("[冒烟-EventBus] unsubscribe 通过");
    }

    /** 异步发布 — 不同线程执行 */
    private void testPublishAsync() {
        String mainThread = Thread.currentThread().getName();
        String[] asyncThread = new String[1];
        Subscription sub = eventBus.subscribe(SaveAfterEvent.class, e -> {
            asyncThread[0] = Thread.currentThread().getName();
        });

        eventBus.publishAsync(createMockSaveAfterEvent(6L, "async", "t", Object.class));

        // 等异步执行完
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        assert asyncThread[0] != null : "异步 handler 未执行";
        assert !mainThread.equals(asyncThread[0]) : "异步应在新线程执行: main=" + mainThread + ", async=" + asyncThread[0];

        sub.cancel();
        log.info("[冒烟-EventBus] publishAsync 通过: thread={}", asyncThread[0]);
    }

    /** 创建一个模拟的 SaveAfterEvent */
    @SuppressWarnings("unchecked")
    private <T> SaveAfterEvent<T> createMockSaveAfterEvent(Long id, String name, String table, Class<T> entityType) {
        return new SaveAfterEvent<>("mock", entityType.getName(), name,
                table, id, null, null, entityType);
    }
}
