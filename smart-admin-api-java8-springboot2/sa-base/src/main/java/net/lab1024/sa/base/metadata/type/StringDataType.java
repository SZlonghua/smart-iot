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

    /** 值校验 — 须为字符串 + maxLength 长度限制 */
    @Override
    public List<String> validateValue(Object value) {
        List<String> errors = new ArrayList<String>();
        if (!(value instanceof String)) {
            errors.add("类型应为 string，实际: " + value);
            return errors;
        }
        if (maxLength != null && ((String) value).length() > maxLength) {
            errors.add("长度不能超过 " + maxLength);
        }
        return errors;
    }
}
