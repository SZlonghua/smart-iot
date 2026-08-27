package net.lab1024.sa.base.metadata.type;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    /** 值校验 — 须为集合，逐元素递归 elementType.validateValue */
    @Override
    public List<String> validateValue(Object value) {
        List<String> errors = new ArrayList<String>();
        if (!(value instanceof Iterable)) {
            errors.add("类型应为 array，实际: " + value);
            return errors;
        }
        if (elementType == null) {
            return errors;    // 定义校验（validate()）已覆盖 elementType 为空
        }
        int i = 0;
        for (Object item : (Iterable<?>) value) {
            final int index = i;
            errors.addAll(
                    elementType.validateValue(item)
                    .stream()
                    .map(e -> "elements[" + index + "] " + e)
                    .collect(Collectors.toList())
            );
            i++;
        }
        return errors;
    }
}
