package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.metadata.type.FloatDataType;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

public class FloatDataTypeCodec implements DataTypeCodec<FloatDataType> {

    @Override public String getTypeName() { return FloatDataType.ID; }

    @Override
    public FloatDataType decode(IJsonNode node) {
        FloatDataType dt = new FloatDataType();
        if (node.has("min")) dt.setMin(node.getDouble("min").floatValue());
        if (node.has("max")) dt.setMax(node.getDouble("max").floatValue());
        if (node.has("unit")) dt.setUnit(node.getString("unit"));
        return dt;
    }

    @Override
    public IJsonNode encode(FloatDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getMin() != null) node.put("min", dt.getMin().doubleValue());
        if (dt.getMax() != null) node.put("max", dt.getMax().doubleValue());
        if (dt.getUnit() != null) node.put("unit", dt.getUnit());
        return node;
    }
}
