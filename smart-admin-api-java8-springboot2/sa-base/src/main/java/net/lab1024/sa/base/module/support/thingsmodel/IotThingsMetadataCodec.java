package net.lab1024.sa.base.module.support.thingsmodel;

import net.lab1024.sa.base.metadata.ParseResult;
import net.lab1024.sa.base.metadata.ThingsMetadata;
import net.lab1024.sa.base.metadata.ThingsMetadataCodec;
import net.lab1024.sa.base.module.support.json.JsonUtil;

import java.util.Collections;
import java.util.List;

/**
 * 物模型编解码器 Iot 实现 —— DCL 单例，纯序列化 + 懒解析
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class IotThingsMetadataCodec implements ThingsMetadataCodec {

    private static volatile IotThingsMetadataCodec INSTANCE;

    private final DataTypeCodecs codecs;

    private IotThingsMetadataCodec() {
        this.codecs = new DataTypeCodecs();
    }

    public static IotThingsMetadataCodec getInstance() {
        if (INSTANCE == null) {
            synchronized (IotThingsMetadataCodec.class) {
                if (INSTANCE == null) {
                    INSTANCE = new IotThingsMetadataCodec();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public ThingsMetadata decode(String json) {
        return new IotThingsMetadata(JsonUtil.parse(json), codecs);
    }

    @Override
    public String encode(ThingsMetadata metadata) {
        return JsonUtil.toJson(((IotThingsMetadata) metadata).root());
    }

    @Override
    public ParseResult parseAndValidate(String json) {
        try {
            ThingsMetadata metadata = decode(json);
            List<String> errors = metadata.validate();
            if (!errors.isEmpty()) {
                return ParseResult.fail(errors);
            }
            return ParseResult.ok(metadata);
        } catch (Exception e) {
            return ParseResult.fail(Collections.singletonList("JSON解析失败: " + e.getMessage()));
        }
    }
}
