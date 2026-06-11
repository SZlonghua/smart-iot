package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.metadata.type.DoubleDataType;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

public class DoubleDataTypeCodec implements DataTypeCodec<DoubleDataType> {

    @Override public String getTypeName() { return DoubleDataType.ID; }

    @Override
    public DoubleDataType decode(IJsonNode node) {
        DoubleDataType dt = new DoubleDataType();
        if (node.has("min")) dt.setMin(node.getDouble("min"));
        if (node.has("max")) dt.setMax(node.getDouble("max"));
        if (node.has("unit")) dt.setUnit(node.getString("unit"));
        return dt;
    }

    @Override
    public IJsonNode encode(DoubleDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getMin() != null) node.put("min", dt.getMin());
        if (dt.getMax() != null) node.put("max", dt.getMax());
        if (dt.getUnit() != null) node.put("unit", dt.getUnit());
        return node;
    }
}
