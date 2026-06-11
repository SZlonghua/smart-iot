package net.lab1024.sa.base.metadata.type;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class DateDataType implements DataType {

    public static final String ID = "date";

    private String format;
    private String tz;

    @Override public String getType() { return ID; }

    @Override
    public List<String> validate() {
        return Collections.emptyList();
    }
}
