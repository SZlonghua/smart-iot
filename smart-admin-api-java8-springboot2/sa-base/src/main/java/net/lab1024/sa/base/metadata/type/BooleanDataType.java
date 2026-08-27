package net.lab1024.sa.base.metadata.type;

import lombok.Data;

import java.util.ArrayList;
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

    /** 值校验 — 须命中 trueValue/falseValue 之一（协议值形态，如 ON/OFF） */
    @Override
    public List<String> validateValue(Object value) {
        List<String> errors = new ArrayList<String>();
        String str = String.valueOf(value);
        if (str.equals(trueValue) || str.equals(falseValue)) {
            return errors;
        }
        errors.add("应为 trueValue[" + trueValue + "] 或 falseValue[" + falseValue + "] 之一，实际: " + value);
        return errors;
    }
}
