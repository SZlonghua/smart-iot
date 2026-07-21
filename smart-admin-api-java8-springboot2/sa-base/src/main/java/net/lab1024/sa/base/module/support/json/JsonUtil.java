package net.lab1024.sa.base.module.support.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON 工具类 —— 静态工具，parse / toJson / createNode
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * JSON 字符串 → JsonNode
     */
    public static IJsonNode parse(String json) {
        try {
            return new IotIJsonNode(MAPPER.readTree(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * JsonNode → JSON 字符串
     */
    public static String toJson(IJsonNode node) {
        try {
            return MAPPER.writeValueAsString(((IotIJsonNode) node).delegate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建空对象 {}
     */
    public static IJsonNode createObjectNode() {
        return new IotIJsonNode(MAPPER.createObjectNode());
    }

    /**
     * 创建空数组 []
     */
    public static IJsonNode createArrayNode() {
        return new IotIJsonNode(MAPPER.createArrayNode());
    }
}
