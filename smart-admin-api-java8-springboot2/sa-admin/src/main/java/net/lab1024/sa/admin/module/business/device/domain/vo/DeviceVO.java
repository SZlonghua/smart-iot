package net.lab1024.sa.admin.module.business.device.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备 VO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class DeviceVO {

    @Schema(description = "设备ID")
    private Long id;

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "Device Key")
    private String deviceKey;

    @Schema(description = "Device Secret")
    private String deviceSecret;

    @Schema(description = "父设备ID")
    private Long parentDeviceId;

    @Schema(description = "产品ID")
    private Long productId;

    @Schema(description = "状态:0离线,1在线,2禁用")
    private Integer status;

    @Schema(description = "最后上线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
