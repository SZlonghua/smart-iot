package net.lab1024.sa.admin.module.business.protocol.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协议 VO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class ProtocolVO {

    @Schema(description = "协议ID")
    private Long id;

    @Schema(description = "协议名称")
    private String name;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "JAR包路径")
    private String jarPath;

    @Schema(description = "协议描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
