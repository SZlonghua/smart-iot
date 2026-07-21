package net.lab1024.sa.base.metadata.type;

import lombok.Data;

/**
 * 枚举元素
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
@Data
public class EnumElement {

    /** 枚举值 */
    private String value;

    /** 显示文本 */
    private String text;

    /** 描述 */
    private String description;
}
