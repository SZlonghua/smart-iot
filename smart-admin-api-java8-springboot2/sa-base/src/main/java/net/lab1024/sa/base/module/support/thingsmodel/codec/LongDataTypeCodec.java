package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.metadata.type.LongDataType;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

public class LongDataTypeCodec implements DataTypeCodec<LongDataType> {

    @Override public String getTypeName() { return LongDataType.ID; }

    @Override
    public LongDataType decode(IJsonNode node) {
        LongDataType dt = new LongDataType();
        if (node.has("min")) dt.setMin(node.getLong("min"));
        if (node.has("max")) dt.setMax(node.getLong("max"));
        if (node.has("unit")) dt.setUnit(node.getString("unit"));
        return dt;
    }

    @Override
    public IJsonNode encode(LongDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getMin() != null) node.put("min", dt.getMin());
        if (dt.getMax() != null) node.put("max", dt.getMax());
        if (dt.getUnit() != null) node.put("unit", dt.getUnit());
        return node;
    }
}
