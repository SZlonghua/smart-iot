package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.metadata.type.DateDataType;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

/**
 * date 类型编解码器
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class DateDataTypeCodec implements DataTypeCodec<DateDataType> {

    @Override
    public String getTypeName() {
        return DateDataType.ID;
    }

    @Override
    public DateDataType decode(IJsonNode node) {
        DateDataType dt = new DateDataType();
        dt.setFormat(node.getString("format"));
        dt.setTz(node.getString("tz"));
        return dt;
    }

    @Override
    public IJsonNode encode(DateDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getFormat() != null) {
            node.put("format", dt.getFormat());
        }
        if (dt.getTz() != null) {
            node.put("tz", dt.getTz());
        }
        return node;
    }
}
