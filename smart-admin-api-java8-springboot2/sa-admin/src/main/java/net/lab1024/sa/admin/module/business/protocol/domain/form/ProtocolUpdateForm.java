package net.lab1024.sa.admin.module.business.protocol.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 协议 更新表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class ProtocolUpdateForm {

    @NotNull(message = "协议ID 不能为空")
    @Schema(description = "协议ID")
    private Long id;

    @NotBlank(message = "协议名称 不能为空")
    @Schema(description = "协议名称")
    private String name;

    @NotBlank(message = "版本号 不能为空")
    @Schema(description = "版本号")
    private String version;

    @Schema(description = "JAR包路径")
    private String jarPath;

    @Schema(description = "协议描述")
    private String description;
}
