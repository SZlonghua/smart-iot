package net.lab1024.sa.base.storage.timeseries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试假通道 — 捕获写入行协议与执行 SQL，并按 SQL 前缀应答预设行。
 * <p>
 * 时序策略测试经本通道注入：断言写入的 LINE 协议与拼装的 SQL（通道即方言边界，真实 TDengine
 * 行为已收敛在 {@link net.lab1024.sa.base.storage.tdengine.TdengineChannel}，不在单测范围）；
 * 应答按注册顺序首个前缀命中，未命中返回空列表（等价通道的"表/列不存在 → 空结果"约定）。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
public class FakeTimeseriesChannel implements TimeseriesChannel {

    private final List<String> lines = new ArrayList<>();
    private final List<String> sqls = new ArrayList<>();
    private final Map<String, List<Map<String, Object>>> cannedByPrefix = new LinkedHashMap<>();

    @Override
    public void initStorage() {
    }

    @Override
    public void writeLines(List<String> lines) {
        this.lines.addAll(lines);
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        sqls.add(sql);
        for (Map.Entry<String, List<Map<String, Object>>> entry : cannedByPrefix.entrySet()) {
            if (sql.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new ArrayList<>();
    }

    /** 注册 SQL 前缀应答（首个注册优先命中；未命中 → 空列表） */
    public void answer(String sqlPrefix, List<Map<String, Object>> rows) {
        cannedByPrefix.put(sqlPrefix, rows);
    }

    public List<String> getLines() {
        return lines;
    }

    public List<String> getSqls() {
        return sqls;
    }
}
