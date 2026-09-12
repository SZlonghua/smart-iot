package net.lab1024.sa.base.storage;

import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.storage.model.StorageData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 存储策略注册器测试 — 构造收集/按 ID 查找/未注册与 null 兜底 EmptyStorageStrategy（永不返回 null）、
 * 重复 ID 后者覆盖、null ID 跳过、空策略列表恒兜底；兜底写入静默、四个查询入口明确抛错。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
class StorageStrategyRegistryTest {

    /** 测试策略 — 继承 EmptyStorageStrategy 仅覆写 ID（兜底类需非 final 才有此继承语义） */
    private static class FakeStrategy extends EmptyStorageStrategy {
        private final String id;

        FakeStrategy(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    @Test
    void registeredStrategyFound() {
        FakeStrategy row = new FakeStrategy("tdengine-row");
        FakeStrategy column = new FakeStrategy("tdengine-column");
        StorageStrategyRegistry registry = new StorageStrategyRegistry(Arrays.asList(row, column));
        assertSame(row, registry.get("tdengine-row"));
        assertSame(column, registry.get("tdengine-column"));
    }

    @Test
    void unregisteredIdFallsBackEmpty() {
        StorageStrategyRegistry registry = new StorageStrategyRegistry(Collections.emptyList());
        assertTrue(registry.get("not-exist") instanceof EmptyStorageStrategy);
        assertTrue(registry.get(null) instanceof EmptyStorageStrategy);
        assertTrue(registry.get("tdengine-row") instanceof EmptyStorageStrategy, "存储未开启无策略 bean 同样兜底");
    }

    @Test
    void duplicateIdLastWins() {
        FakeStrategy first = new FakeStrategy("dup");
        FakeStrategy second = new FakeStrategy("dup");
        StorageStrategyRegistry registry = new StorageStrategyRegistry(Arrays.asList(first, second));
        assertSame(second, registry.get("dup"), "重复 ID 后者覆盖并告警");
    }

    @Test
    void nullIdStrategySkipped() {
        StorageStrategyRegistry registry = new StorageStrategyRegistry(
                Collections.singletonList(new FakeStrategy(null)));
        assertTrue(registry.get("any") instanceof EmptyStorageStrategy, "null ID 不注册，查找仍兜底");
    }

    @Test
    void emptySaveSilentNoThrow() {
        StorageStrategy empty = new EmptyStorageStrategy();
        empty.save(new StorageData() {
        });
        // 不抛异常即通过（写入仅告警一次）
    }

    @Test
    void emptyReadMethodsThrowReadError() {
        StorageStrategy empty = new EmptyStorageStrategy();
        // 四个查询入口统一抛业务异常并带存储未开启提示
        assertReadError(() -> empty.queryPropertyHistory(null));
        assertReadError(() -> empty.queryLatestPropertyValues(null));
        assertReadError(() -> empty.queryEventHistory(null));
        assertReadError(() -> empty.queryCommandLogHistory(null));
        assertEquals(null, empty.getId());
    }

    private static void assertReadError(Runnable action) {
        BusinessException exception = assertThrows(BusinessException.class, action::run);
        assertTrue(exception.getMessage().contains("设备消息数据存储未开启或无匹配存储策略"),
                "报错信息应提示存储未开启: " + exception.getMessage());
    }
}
