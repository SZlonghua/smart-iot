package net.lab1024.sa.admin.module.business.alarmlog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警日志 实体类
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@TableName("alarm_log")
public class AlarmLogEntity {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备Key
     */
    private String deviceKey;

    /**
     * 告警级别
     */
    private Integer level;

    /**
     * 告警描述
     */
    private String description;

    /**
     * 处理状态
     */
    private Integer status;

    /**
     * 触发时间
     */
    private LocalDateTime triggerTime;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理备注
     */
    private String handleNote;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
