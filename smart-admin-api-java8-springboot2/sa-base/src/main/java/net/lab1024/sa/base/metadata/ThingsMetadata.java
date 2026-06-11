package net.lab1024.sa.base.metadata;

import java.util.List;

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
}
