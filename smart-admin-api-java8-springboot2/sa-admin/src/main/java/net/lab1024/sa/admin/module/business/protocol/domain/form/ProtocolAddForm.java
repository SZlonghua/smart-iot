package net.lab1024.sa.admin.module.business.protocol.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 协议 新建表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class ProtocolAddForm {

    @NotBlank(message = "协议名称 不能为空")
    @Schema(description = "协议名称")
    private String name;

    @NotBlank(message = "版本号 不能为空")
    @Schema(description = "版本号")
    private String version;

    @NotBlank(message = "加载方式 不能为空")
    @Schema(description = "加载方式(jar/local)")
    private String loader;

    @Schema(description = "JAR包路径")
    private String jarPath;

    @Schema(description = "JAR包原始文件名")
    private String jarName;

    @Schema(description = "协议描述")
    private String description;
}
