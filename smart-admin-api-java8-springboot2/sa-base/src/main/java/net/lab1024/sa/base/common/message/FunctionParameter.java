package net.lab1024.sa.base.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 功能调用参数。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/16
 * &#064;Copyright  1024创新实验室
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionParameter implements Serializable {

    /** 参数名 */
    private String name;

    /** 参数值 */
    private Object value;
}
