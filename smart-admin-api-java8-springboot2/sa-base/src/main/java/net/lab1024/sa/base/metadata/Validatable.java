package net.lab1024.sa.base.metadata;

import java.util.List;

/**
 * 可校验接口
 *
 * @Author 廖涛
 * @Date 2026/06/11
 * @Copyright 1024创新实验室
 */
public interface Validatable {

    /** 校验自身，空列表 = 合法 */
    List<String> validate();
}
