package net.lab1024.sa.base.module.support.thingsmodel.codec;

import net.lab1024.sa.base.metadata.type.DataTypeCodec;
import net.lab1024.sa.base.metadata.type.EnumDataType;
import net.lab1024.sa.base.metadata.type.EnumElement;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * enum 类型编解码器
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class EnumDataTypeCodec implements DataTypeCodec<EnumDataType> {

    @Override
    public String getTypeName() {
        return EnumDataType.ID;
    }

    @Override
    public EnumDataType decode(IJsonNode node) {
        EnumDataType dt = new EnumDataType();
        IJsonNode elementsNode = node.get("elements");
        if (elementsNode != null && elementsNode.isArray()) {
            List<EnumElement> elements = new ArrayList<EnumElement>();
            for (int i = 0; i < elementsNode.size(); i++) {
                IJsonNode elemNode = elementsNode.get(i);
                if (elemNode != null) {
                    EnumElement elem = new EnumElement();
                    elem.setValue(elemNode.getString("value"));
                    elem.setText(elemNode.getString("text"));
                    elem.setDescription(elemNode.getString("description"));
                    elements.add(elem);
                }
            }
            dt.setElements(elements);
        }
        return dt;
    }

    @Override
    public IJsonNode encode(EnumDataType dt) {
        IJsonNode node = JsonUtil.createObjectNode();
        node.put("type", dt.getType());
        if (dt.getElements() != null) {
            IJsonNode elementsArr = JsonUtil.createArrayNode();
            for (EnumElement elem : dt.getElements()) {
                IJsonNode elemNode = JsonUtil.createObjectNode();
                elemNode.put("value", elem.getValue());
                elemNode.put("text", elem.getText());
                if (elem.getDescription() != null) {
                    elemNode.put("description", elem.getDescription());
                }
                elementsArr.add(elemNode);
            }
            node.put("elements", elementsArr);
        }
        return node;
    }
}
