package net.lab1024.sa.base.metadata;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 解析结果
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
@Data
public class ParseResult {

    /** 解析结果（失败时为 null） */
    private ThingsMetadata metadata;

    /** 校验错误（空 = 合法） */
    private List<String> errors;

    /** 是否合法 */
    public boolean isValid() {
        return metadata != null && (errors == null || errors.isEmpty());
    }

    /** 创建失败结果 */
    public static ParseResult fail(List<String> errors) {
        ParseResult r = new ParseResult();
        r.metadata = null;
        r.errors = errors;
        return r;
    }

    /** 创建成功结果 */
    public static ParseResult ok(ThingsMetadata metadata) {
        ParseResult r = new ParseResult();
        r.metadata = metadata;
        r.errors = Collections.emptyList();
        return r;
    }
}
