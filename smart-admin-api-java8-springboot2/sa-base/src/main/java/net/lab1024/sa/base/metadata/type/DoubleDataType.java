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
}
