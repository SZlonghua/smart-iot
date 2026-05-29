package net.lab1024.sa.admin.module.business.networkcomponent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网络组件 实体类
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@TableName("network_component")
public class NetworkComponentEntity {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件类型
     */
    private String type;

    /**
     * 组件配置(JSON)
     */
    private String configuration;

    /**
     * 启用状态 1:启用 0:禁用
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
