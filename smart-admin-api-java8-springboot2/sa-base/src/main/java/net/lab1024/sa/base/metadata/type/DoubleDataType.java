package net.lab1024.sa.base.metadata.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoubleDataType extends NumberDataType {

    public static final String ID = "double";

    private Double min;
    private Double max;

    @Override public String getType() { return ID; }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        if (min != null && max != null && min > max) {
            errors.add("min不能大于max");
        }
        return errors;
    }

    /** 值校验 — 须为数字 + Double min/max 范围 */
    @Override
    public List<String> validateValue(Object value) {
        List<String> errors = new ArrayList<String>();
        if (!(value instanceof Number)) {
            errors.add("类型应为 double，实际: " + value);
            return errors;
        }
        double v = ((Number) value).doubleValue();
        if (min != null && v < min) {
            errors.add("不能小于 " + min);
        }
        if (max != null && v > max) {
            errors.add("不能大于 " + max);
        }
        return errors;
    }
}
