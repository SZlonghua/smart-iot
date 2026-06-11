package net.lab1024.sa.base.metadata;

/**
 * JSON 序列化接口
 *
 * @Author 廖涛
 * @Date 2026/06/11
 * @Copyright 1024创新实验室
 */
public interface Jsonable {

    /** 序列化为 JSON 字符串 */
    String toJson();

    /** 从 JSON 字符串反序列化，重建自身状态 */
    void fromJson(String json);
}
