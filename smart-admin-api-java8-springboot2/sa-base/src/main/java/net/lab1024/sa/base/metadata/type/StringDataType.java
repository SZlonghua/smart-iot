package net.lab1024.sa.base.metadata.type;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StringDataType implements DataType {

    public static final String ID = "string";

    private Integer maxLength;

    @Override public String getType() { return ID; }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        if (maxLength != null && maxLength <= 0) {
            errors.add("maxLength必须大于0");
        }
        return errors;
    }
}
