package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.metadata.type.IntDataType;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

public class IntDataTypeCodec implements DataTypeCodec<IntDataType> {

    @Override public String getTypeName() { return IntDataType.ID; }

    @Override
    public IntDataType decode(IJsonNode node) {
        IntDataType dt = new IntDataType();
        if (node.has("min")) dt.setMin(node.getInt("min"));
        if (node.has("max")) dt.setMax(node.getInt("max"));
        if (node.has("unit")) dt.setUnit(node.getString("unit"));
        return dt;
    }

    @Override
    public IJsonNode encode(IntDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getMin() != null) node.put("min", dt.getMin());
        if (dt.getMax() != null) node.put("max", dt.getMax());
        if (dt.getUnit() != null) node.put("unit", dt.getUnit());
        return node;
    }
}
