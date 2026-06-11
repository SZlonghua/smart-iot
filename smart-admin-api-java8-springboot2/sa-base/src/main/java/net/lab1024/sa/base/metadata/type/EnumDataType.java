package net.lab1024.sa.base.metadata.type;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class EnumDataType implements DataType {

    public static final String ID = "enum";

    private List<EnumElement> elements;

    @Override public String getType() { return ID; }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        if (elements == null || elements.isEmpty()) {
            errors.add("elements不能为空");
        } else {
            Set<String> values = new HashSet<String>();
            for (int i = 0; i < elements.size(); i++) {
                String value = elements.get(i).getValue();
                if (value == null || value.isEmpty()) {
                    errors.add("elements[" + i + "]: value不能为空");
                } else if (!values.add(value)) {
                    errors.add("elements[" + i + "]: value重复: " + value);
                }
            }
        }
        return errors;
    }
}
