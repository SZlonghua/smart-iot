package net.lab1024.sa.base.metadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 物模型元数据接口 —— 聚合根
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public interface ThingsMetadata extends Metadata, Jsonable {

    List<? extends PropertyMetadata> getProperties();
    List<? extends FunctionMetadata> getFunctions();
    List<? extends EventMetadata> getEvents();

    PropertyMetadata getPropertyOrNull(String id);
    FunctionMetadata getFunctionOrNull(String id);
    EventMetadata getEventOrNull(String id);

    /** 校验读取的属性 — 预留：返回空列表（实现见物模型校验设计.md，后续落地） */
    default List<String> validateReadProperties(List<String> properties) {
        return Collections.emptyList();
    }

    /** 校验写入的属性值 — 预留：返回空列表（实现见物模型校验设计.md，后续落地） */
    default List<String> validateProperties(Map<String, Object> properties) {
        return Collections.emptyList();
    }

    /** 校验功能调用参数 — 预留：返回空列表（实现见物模型校验设计.md，后续落地） */
    default List<String> validateFunction(String functionId, Map<String, Object> params) {
        return Collections.emptyList();
    }
}
