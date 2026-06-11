package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.BooleanDataType;
import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

/**
 * boolean 类型编解码器
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class BooleanDataTypeCodec implements DataTypeCodec<BooleanDataType> {

    @Override
    public String getTypeName() {
        return BooleanDataType.ID;
    }

    @Override
    public BooleanDataType decode(IJsonNode node) {
        BooleanDataType dt = new BooleanDataType();
        dt.setTrueText(node.getString("trueText"));
        dt.setFalseText(node.getString("falseText"));
        dt.setTrueValue(node.getString("trueValue"));
        dt.setFalseValue(node.getString("falseValue"));
        return dt;
    }

    @Override
    public IJsonNode encode(BooleanDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getTrueText() != null) {
            node.put("trueText", dt.getTrueText());
        }
        if (dt.getFalseText() != null) {
            node.put("falseText", dt.getFalseText());
        }
        if (dt.getTrueValue() != null) {
            node.put("trueValue", dt.getTrueValue());
        }
        if (dt.getFalseValue() != null) {
            node.put("falseValue", dt.getFalseValue());
        }
        return node;
    }
}
