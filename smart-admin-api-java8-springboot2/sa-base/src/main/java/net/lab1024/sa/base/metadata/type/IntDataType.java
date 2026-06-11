package net.lab1024.sa.base.metadata.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntDataType extends NumberDataType {

    public static final String ID = "int";

    private Integer min;
    private Integer max;

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
