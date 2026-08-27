package net.lab1024.sa.base.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /** 校验读取的属性 — 每个属性需在物模型中存在，返回错误列表（空 = 合法） */
    default List<String> validateReadProperties(List<String> properties) {
        List<String> errors = new ArrayList<>();
        for (String propertyId : properties) {
            if (getPropertyOrNull(propertyId) == null) {
                errors.add("属性 [" + propertyId + "] 不存在");
            }
        }
        return errors;
    }

    /** 校验写入的属性值 — 属性存在 + 非只读 + 值按各自 DataType.validateValue 递归校验，返回错误列表 */
    default List<String> validateProperties(Map<String, Object> properties) {
        List<String> errors = new ArrayList<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            PropertyMetadata property = getPropertyOrNull(entry.getKey());
            if (property == null) {
                errors.add("属性 [" + entry.getKey() + "] 不存在");
                continue;
            }
            // 只读（accessMode=r）不可写；w/rw 可写（null 视为可写）
            if ("r".equals(property.getAccessMode())) {
                errors.add("属性 [" + entry.getKey() + "] 为只读，不可写");
                continue;
            }
            // 递归：值按属性的值类型校验（int 范围 / bool 类型 / enum 枚举值 / object 子属性等）
            errors.addAll(prefix("属性 [" + entry.getKey() + "]", property.getValueType().validateValue(entry.getValue())));
        }
        return errors;
    }

    /** 校验上报的属性值 — 属性存在 + 值按各自 DataType.validateValue 递归校验（不做只读限制：accessMode=r 仅限制平台下发，设备上报允许） */
    default List<String> validateReportProperties(Map<String, Object> properties) {
        List<String> errors = new ArrayList<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            PropertyMetadata property = getPropertyOrNull(entry.getKey());
            if (property == null) {
                errors.add("属性 [" + entry.getKey() + "] 不存在");
                continue;
            }
            // 递归：值按属性的值类型校验（与 validateProperties 一致，仅缺只读检查）
            errors.addAll(prefix("属性 [" + entry.getKey() + "]", property.getValueType().validateValue(entry.getValue())));
        }
        return errors;
    }

    /** 校验上报的事件 — 事件存在 + 数据按事件值类型 validateValue 递归校验，返回错误列表 */
    default List<String> validateEvent(String event, Object data) {
        List<String> errors = new ArrayList<>();
        EventMetadata metadata = getEventOrNull(event);
        if (metadata == null) {
            errors.add("事件 [" + event + "] 不存在");
            return errors;
        }
        errors.addAll(prefix("事件 [" + event + "]", metadata.getValueType().validateValue(data)));
        return errors;
    }

    /** 校验功能调用参数 — 功能存在 + 参数名匹配 + 必填齐全 + 值按各自 DataType.validateValue 递归校验，返回错误列表 */
    default List<String> validateFunction(String functionId, Map<String, Object> params) {
        List<String> errors = new ArrayList<>();
        FunctionMetadata function = getFunctionOrNull(functionId);
        if (function == null) {
            errors.add("功能 [" + functionId + "] 不存在");
            return errors;
        }
        Map<String, FunctionParamMetadata> inputs = function.getInputs().stream()
                .collect(Collectors.toMap(FunctionParamMetadata::getId, p -> p));
        // ① 参数名校验 + 值校验（递归）
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            FunctionParamMetadata param = inputs.get(entry.getKey());
            if (param == null) {
                errors.add("功能 [" + functionId + "] 不存在参数 [" + entry.getKey() + "]");
                continue;
            }
            errors.addAll(prefix("参数 [" + entry.getKey() + "]", param.getValueType().validateValue(entry.getValue())));
        }
        // ② 必填参数齐全
        for (FunctionParamMetadata param : function.getInputs()) {
            if (param.isRequired() && !params.containsKey(param.getId())) {
                errors.add("功能 [" + functionId + "] 缺少必填参数 [" + param.getId() + "]");
            }
        }
        return errors;
    }

    /** 错误信息统一加前缀（指明是哪个属性/参数） */
    default List<String> prefix(String name, List<String> errors) {
        return errors.stream().map(e -> name + " " + e).collect(Collectors.toList());
    }
}
