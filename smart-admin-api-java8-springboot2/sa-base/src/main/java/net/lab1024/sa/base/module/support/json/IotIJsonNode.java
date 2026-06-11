package net.lab1024.sa.base.module.support.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JsonNode 包私有实现，内部持有 Jackson JsonNode，外部不可见
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
class IotIJsonNode implements IJsonNode {

    final JsonNode delegate;

    IotIJsonNode(JsonNode delegate) {
        this.delegate = delegate;
    }

    // ===== 读取 =====
    @Override
    public String getString(String field) {
        JsonNode child = delegate.get(field);
        return (child == null || child.isNull()) ? null : child.asText();
    }

    @Override
    public Integer getInt(String field) {
        JsonNode child = delegate.get(field);
        return (child == null || child.isNull()) ? null : child.asInt();
    }

    @Override
    public Double getDouble(String field) {
        JsonNode child = delegate.get(field);
        return (child == null || child.isNull()) ? null : child.asDouble();
    }

    @Override
    public Boolean getBoolean(String field) {
        JsonNode child = delegate.get(field);
        return (child == null || child.isNull()) ? null : child.asBoolean();
    }

    @Override
    public Long getLong(String field) {
        JsonNode child = delegate.get(field);
        return (child == null || child.isNull()) ? null : child.asLong();
    }

    @Override
    public IJsonNode get(String field) {
        JsonNode child = delegate.get(field);
        return child == null ? null : new IotIJsonNode(child);
    }

    // ===== 数组 =====
    @Override
    public boolean isArray() {
        return delegate.isArray();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public IJsonNode get(int index) {
        if (index < 0 || index >= delegate.size()) {
            return null;
        }
        return new IotIJsonNode(delegate.get(index));
    }

    // ===== 写入 =====
    @Override
    public void put(String field, String value) {
        ((ObjectNode) delegate).put(field, value);
    }

    @Override
    public void put(String field, int value) {
        ((ObjectNode) delegate).put(field, value);
    }

    @Override
    public void put(String field, double value) {
        ((ObjectNode) delegate).put(field, value);
    }

    @Override
    public void put(String field, boolean value) {
        ((ObjectNode) delegate).put(field, value);
    }

    @Override
    public void put(String field, long value) {
        ((ObjectNode) delegate).put(field, value);
    }

    @Override
    public void put(String field, IJsonNode value) {
        if (value == null) {
            ((ObjectNode) delegate).putNull(field);
        } else {
            ((ObjectNode) delegate).set(field, ((IotIJsonNode) value).delegate);
        }
    }

    @Override
    public void add(IJsonNode value) {
        if (value == null) {
            ((ArrayNode) delegate).addNull();
        } else {
            ((ArrayNode) delegate).add(((IotIJsonNode) value).delegate);
        }
    }

    // ===== 基本 =====
    @Override
    public String asText() {
        return delegate.asText();
    }

    @Override
    public boolean isNull() {
        return delegate == null || delegate.isNull();
    }

    @Override
    public boolean has(String field) {
        return delegate.has(field) && !delegate.get(field).isNull();
    }
}
