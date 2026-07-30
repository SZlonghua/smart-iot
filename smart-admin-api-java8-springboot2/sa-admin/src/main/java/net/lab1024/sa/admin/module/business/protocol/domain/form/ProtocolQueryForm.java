package net.lab1024.sa.admin.module.business.protocol.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 协议 查询表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProtocolQueryForm extends PageParam {

    @Schema(description = "协议名称")
    private String name;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "加载方式(jar/local)")
    private String loader;

    @Schema(description = "JAR包路径")
    private String jarPath;

    @Schema(description = "协议描述")
    private String description;
}
