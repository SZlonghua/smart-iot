package net.lab1024.sa.base.module.support.thingsmodel;

import net.lab1024.sa.base.metadata.FunctionMetadata;
import net.lab1024.sa.base.metadata.FunctionParamMetadata;
import net.lab1024.sa.base.metadata.type.DataType;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IotFunctionMetadata implements FunctionMetadata {

    private IJsonNode node;
    private DataTypeCodecs codecs;

    private String id;
    private String name;
    private boolean async;
    private String description;
    private List<IotFunctionParamMetadata> inputs;
    private DataType output;

    public IotFunctionMetadata(IJsonNode node, DataTypeCodecs codecs) {
        this.node = node;
        this.codecs = codecs;
        this.id = node.getString("id");
        this.name = node.getString("name");
        Boolean a = node.getBoolean("async");
        this.async = a != null && a;
        this.description = node.getString("description");
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public boolean isAsync() { return async; }
    @Override public String getDescription() { return description; }

    @Override
    public List<? extends FunctionParamMetadata> getInputs() {
        if (inputs == null) {
            inputs = new ArrayList<IotFunctionParamMetadata>();
            IJsonNode arr = node.get("inputs");
            if (arr != null && arr.isArray()) {
                for (int i = 0; i < arr.size(); i++) {
                    IJsonNode in = arr.get(i);
                    if (in != null) inputs.add(new IotFunctionParamMetadata(in, codecs));
                }
            }
        }
        return inputs;
    }

    @Override
    public DataType getOutput() {
        if (output == null) {
            output = codecs.decode(node.get("output"));
        }
        return output;
    }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        if (id == null || id.isEmpty()) errors.add("id不能为空");
        if (name == null || name.isEmpty()) errors.add("name不能为空");
        List<? extends FunctionParamMetadata> inputs = getInputs();
        if (!inputs.isEmpty()) {
            Set<String> inputIds = new HashSet<String>();
            for (int j = 0; j < inputs.size(); j++) {
                FunctionParamMetadata param = inputs.get(j);
                if (!inputIds.add(param.getId())) {
                    errors.add("inputs[" + j + "]: id重复: " + param.getId());
                }
                errors.addAll(param.validate());
            }
        }
        if (getOutput() != null) {
            errors.addAll(getOutput().validate());
        }
        return errors;
    }

    @Override
    public String toJson() { return JsonUtil.toJson(node); }

    @Override
    public void fromJson(String json) {
        this.node = JsonUtil.parse(json);
        this.id = node.getString("id");
        this.name = node.getString("name");
        Boolean a = node.getBoolean("async");
        this.async = a != null && a;
        this.description = node.getString("description");
        this.output = null;
        this.inputs = null;
        if (this.codecs == null) this.codecs = new DataTypeCodecs();
    }
}
