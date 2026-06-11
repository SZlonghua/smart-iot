package net.lab1024.sa.base.metadata;

/**
 * 物模型编解码器接口 —— 只负责序列化
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public interface ThingsMetadataCodec {

    /** JSON → ThingsMetadata */
    ThingsMetadata decode(String json);

    /** ThingsMetadata → JSON */
    String encode(ThingsMetadata metadata);

    /** 解析 + 校验 + 归一化 */
    ParseResult parseAndValidate(String json);
}
