package net.lab1024.sa.base.metadata.type;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ObjectDataType implements DataType {

    public static final String ID = "object";

    private List<ObjectProperty> properties;

    @Override public String getType() { return ID; }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        if (properties == null || properties.isEmpty()) {
            errors.add("properties不能为空");
        } else {
            Set<String> ids = new HashSet<String>();
            for (int i = 0; i < properties.size(); i++) {
                ObjectProperty prop = properties.get(i);
                if (prop.getId() == null || prop.getId().isEmpty()) {
                    errors.add("properties[" + i + "]: id不能为空");
                } else if (!ids.add(prop.getId())) {
                    errors.add("properties[" + i + "]: id重复: " + prop.getId());
                }
                if (prop.getValueType() == null) {
                    errors.add("properties[" + i + "]: valueType不能为空");
                } else {
                    errors.addAll(prop.getValueType().validate());
                }
            }
        }
        return errors;
    }
}
