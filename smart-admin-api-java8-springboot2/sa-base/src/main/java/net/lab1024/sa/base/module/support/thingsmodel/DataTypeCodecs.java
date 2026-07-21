package net.lab1024.sa.base.module.support.thingsmodel;

import net.lab1024.sa.base.metadata.type.DataType;
import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.thingsmodel.codec.ArrayDataTypeCodec;
import net.lab1024.sa.base.module.support.thingsmodel.codec.BooleanDataTypeCodec;
import net.lab1024.sa.base.module.support.thingsmodel.codec.DateDataTypeCodec;
import net.lab1024.sa.base.module.support.thingsmodel.codec.DoubleDataTypeCodec;
import net.lab1024.sa.base.module.support.thingsmodel.codec.EnumDataTypeCodec;
import net.lab1024.sa.base.module.support.thingsmodel.codec.FloatDataTypeCodec;
import net.lab1024.sa.base.module.support.thingsmodel.codec.IntDataTypeCodec;
import net.lab1024.sa.base.module.support.thingsmodel.codec.LongDataTypeCodec;
import net.lab1024.sa.base.module.support.thingsmodel.codec.ObjectDataTypeCodec;
import net.lab1024.sa.base.module.support.thingsmodel.codec.StringDataTypeCodec;

import java.util.HashMap;
import java.util.Map;

/**
 * DataType 编解码器集合 —— 注册全部 codec，对外提供统一的 decode/encode（序列化门面）
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class DataTypeCodecs {

    private final Map<String, DataTypeCodec<?>> map = new HashMap<String, DataTypeCodec<?>>();

    public DataTypeCodecs() {
        register(new IntDataTypeCodec());
        register(new FloatDataTypeCodec());
        register(new DoubleDataTypeCodec());
        register(new LongDataTypeCodec());
        register(new StringDataTypeCodec());
        register(new BooleanDataTypeCodec());
        register(new EnumDataTypeCodec());
        register(new DateDataTypeCodec());
        register(new ArrayDataTypeCodec(this));
        register(new ObjectDataTypeCodec(this));
    }

    private void register(DataTypeCodec<?> codec) {
        map.put(codec.getTypeName(), codec);
    }

    public DataTypeCodec<?> get(String typeName) {
        return map.get(typeName);
    }

    // ==================== 序列化门面 ====================

    /**
     * IJsonNode → DataType，自动根据 type 字段定位 codec 并解码
     */
    @SuppressWarnings("unchecked")
    public DataType decode(IJsonNode node) {
        if (node == null) {
            return null;
        }
        String typeName = node.getString("type");
        if (typeName == null) {
            return null;
        }
        DataTypeCodec<?> codec = get(typeName);
        if (codec == null) {
            return null;
        }
        return codec.decode(node);
    }

    /**
     * DataType → IJsonNode，自动根据 getType() 定位 codec 并编码
     */
    @SuppressWarnings("unchecked")
    public IJsonNode encode(DataType dt) {
        DataTypeCodec codec = get(dt.getType());
        if (codec == null) {
            return null;
        }
        return codec.encode(dt);
    }
}
