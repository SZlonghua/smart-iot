package net.lab1024.sa.base.metadata.type;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    /** 值校验 — 字符串按 format（默认 yyyy-MM-dd HH:mm:ss）解析；数字视为时间戳（秒/毫秒均可，format 仅前端展示用） */
    @Override
    public List<String> validateValue(Object value) {
        List<String> errors = new ArrayList<String>();
        if (value instanceof Number) {
            // 时间戳校验：须为有限非负数（1970 前无意义，负数视为非法）
            double v = ((Number) value).doubleValue();
            if (Double.isNaN(v) || Double.isInfinite(v) || v < 0) {
                errors.add("时间戳不合法，实际: " + value);
            }
            return errors;
        }
        if (!(value instanceof String)) {
            errors.add("类型应为 date 字符串或时间戳，实际: " + value);
            return errors;
        }
        String pattern = StringUtils.isNotBlank(format) ? format : "yyyy-MM-dd HH:mm:ss";
        try {
            new SimpleDateFormat(pattern).parse((String) value);
        } catch (ParseException e) {
            errors.add("日期格式不合法，应为: " + pattern + "，实际: " + value);
        }
        return errors;
    }
}
