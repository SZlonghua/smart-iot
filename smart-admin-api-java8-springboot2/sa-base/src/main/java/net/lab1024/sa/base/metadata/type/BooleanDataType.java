package net.lab1024.sa.base.metadata.type;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class BooleanDataType implements DataType {

    public static final String ID = "boolean";

    private String trueText;
    private String falseText;
    private String trueValue;
    private String falseValue;

    @Override public String getType() { return ID; }

    @Override
    public List<String> validate() {
        return Collections.emptyList();
    }
}
