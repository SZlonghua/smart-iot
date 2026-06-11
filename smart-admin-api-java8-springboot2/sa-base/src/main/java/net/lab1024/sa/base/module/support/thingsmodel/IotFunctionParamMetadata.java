package net.lab1024.sa.base.module.support.thingsmodel;

import net.lab1024.sa.base.metadata.FunctionParamMetadata;
import net.lab1024.sa.base.metadata.type.DataType;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class IotFunctionParamMetadata implements FunctionParamMetadata {

    private IJsonNode node;
    private final DataTypeCodecs codecs;

    private String id;
    private String name;
    private boolean required;
    private DataType valueType;

    public IotFunctionParamMetadata(IJsonNode node, DataTypeCodecs codecs) {
        this.node = node;
        this.codecs = codecs;
        this.id = node.getString("id");
        this.name = node.getString("name");
        Boolean r = node.getBoolean("required");
        this.required = r != null && r;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public boolean isRequired() { return required; }

    @Override
    public DataType getValueType() {
        if (valueType == null) {
            valueType = codecs.decode(node.get("valueType"));
        }
        return valueType;
    }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        if (id == null || id.isEmpty()) errors.add("id不能为空");
        if (name == null || name.isEmpty()) errors.add("name不能为空");
        if (getValueType() == null) errors.add("valueType不能为空");
        else errors.addAll(getValueType().validate());
        return errors;
    }

}
