package net.lab1024.sa.base.metadata.type;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class ArrayDataType implements DataType {

    public static final String ID = "array";

    private DataType elementType;

    @Override public String getType() { return ID; }

    @Override
    public List<String> validate() {
        if (elementType == null) {
            return Collections.singletonList("elementType不能为空");
        }
        return elementType.validate();
    }
}
