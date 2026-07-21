package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.metadata.type.StringDataType;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

/**
 * string 类型编解码器
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class StringDataTypeCodec implements DataTypeCodec<StringDataType> {

    @Override
    public String getTypeName() {
        return StringDataType.ID;
    }

    @Override
    public StringDataType decode(IJsonNode node) {
        StringDataType dt = new StringDataType();
        if (node.has("maxLength")) {
            dt.setMaxLength(node.getInt("maxLength"));
        }
        return dt;
    }

    @Override
    public IJsonNode encode(StringDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getMaxLength() != null) {
            node.put("maxLength", dt.getMaxLength());
        }
        return node;
    }
}
