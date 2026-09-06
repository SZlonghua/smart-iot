package net.lab1024.sa.base.module.support.thingsmodel;

import net.lab1024.sa.base.metadata.EventMetadata;
import net.lab1024.sa.base.metadata.type.DataType;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IotEventMetadata implements EventMetadata {

    /** 合法事件类型（与 EventMetadata.getType 语义一致） */
    private static final List<String> VALID_EVENT_TYPES = Arrays.asList("info", "warning", "error");

    private IJsonNode node;
    private DataTypeCodecs codecs;

    private String id;
    private String name;
    private String type;
    private String description;
    private DataType valueType;

    public IotEventMetadata(IJsonNode node, DataTypeCodecs codecs) {
        this.node = node;
        this.codecs = codecs;
        this.id = node.getString("id");
        this.name = node.getString("name");
        this.type = node.getString("type");
        this.description = node.getString("description");
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public String getType() { return type; }
    @Override public String getDescription() { return description; }

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
        if (type == null || type.isEmpty()) {
            errors.add("type不能为空");
        } else if (!VALID_EVENT_TYPES.contains(type)) {
            errors.add("type不合法（info/warning/error）");
        }
        if (getValueType() == null) errors.add("valueType不能为空");
        else errors.addAll(getValueType().validate());
        return errors;
    }

    @Override
    public String toJson() { return JsonUtil.toJson(node); }

    @Override
    public void fromJson(String json) {
        this.node = JsonUtil.parse(json);
        this.id = node.getString("id");
        this.name = node.getString("name");
        this.type = node.getString("type");
        this.description = node.getString("description");
        this.valueType = null;
        if (this.codecs == null) this.codecs = new DataTypeCodecs();
    }
}
