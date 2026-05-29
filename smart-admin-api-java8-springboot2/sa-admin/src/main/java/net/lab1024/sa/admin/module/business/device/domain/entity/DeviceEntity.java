package net.lab1024.sa.admin.module.business.device.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备 实体类
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@TableName("device")
public class DeviceEntity {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 名称 */
    private String name;

    /** Device Key */
    private String deviceKey;

    /** Device Secret */
    private String deviceSecret;

    /** 父设备ID */
    private Long parentDeviceId;

    /** 产品ID */
    private Long productId;

    /** 状态 */
    private Integer status;

    /** 最后上线时间 */
    private LocalDateTime lastOnlineTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
