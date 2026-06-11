package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.metadata.type.ObjectDataType;
import net.lab1024.sa.base.metadata.type.ObjectProperty;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;
import net.lab1024.sa.base.module.support.thingsmodel.DataTypeCodecs;

import java.util.ArrayList;
import java.util.List;

/**
 * object 类型编解码器 —— 持有 DataTypeCodecs，递归处理子属性
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class ObjectDataTypeCodec implements DataTypeCodec<ObjectDataType> {

    private final DataTypeCodecs codecs;

    public ObjectDataTypeCodec(DataTypeCodecs codecs) {
        this.codecs = codecs;
    }

    @Override
    public String getTypeName() {
        return ObjectDataType.ID;
    }

    @Override
    public ObjectDataType decode(IJsonNode node) {
        ObjectDataType dt = new ObjectDataType();
        IJsonNode propsNode = node.get("properties");
        if (propsNode != null && propsNode.isArray()) {
            List<ObjectProperty> properties = new ArrayList<ObjectProperty>();
            for (int i = 0; i < propsNode.size(); i++) {
                IJsonNode propNode = propsNode.get(i);
                if (propNode != null) {
                    ObjectProperty prop = new ObjectProperty();
                    prop.setId(propNode.getString("id"));
                    prop.setName(propNode.getString("name"));
                    prop.setValueType(codecs.decode(propNode.get("valueType")));
                    properties.add(prop);
                }
            }
            dt.setProperties(properties);
        }
        return dt;
    }

    @Override
    public IJsonNode encode(ObjectDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getProperties() != null) {
            IJsonNode propsArr = JsonUtil.createArrayNode();
            for (ObjectProperty prop : dt.getProperties()) {
                IJsonNode propNode = JsonUtil.createObjectNode();
                propNode.put("id", prop.getId());
                propNode.put("name", prop.getName());
                if (prop.getValueType() != null) {
                    propNode.put("valueType", codecs.encode(prop.getValueType()));
                }
                propsArr.add(propNode);
            }
            node.put("properties", propsArr);
        }
        return node;
    }
}
