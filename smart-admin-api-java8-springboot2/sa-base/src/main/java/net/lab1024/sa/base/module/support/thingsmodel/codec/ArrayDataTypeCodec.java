package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.ArrayDataType;
import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;
import net.lab1024.sa.base.module.support.thingsmodel.DataTypeCodecs;

/**
 * array 类型编解码器 —— 持有 DataTypeCodecs，递归处理 elementType
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class ArrayDataTypeCodec implements DataTypeCodec<ArrayDataType> {

    private final DataTypeCodecs codecs;

    public ArrayDataTypeCodec(DataTypeCodecs codecs) {
        this.codecs = codecs;
    }

    @Override
    public String getTypeName() {
        return ArrayDataType.ID;
    }

    @Override
    public ArrayDataType decode(IJsonNode node) {
        ArrayDataType dt = new ArrayDataType();
        dt.setElementType(codecs.decode(node.get("elementType")));
        return dt;
    }

    @Override
    public IJsonNode encode(ArrayDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getElementType() != null) {
            node.put("elementType", codecs.encode(dt.getElementType()));
        }
        return node;
    }
}
