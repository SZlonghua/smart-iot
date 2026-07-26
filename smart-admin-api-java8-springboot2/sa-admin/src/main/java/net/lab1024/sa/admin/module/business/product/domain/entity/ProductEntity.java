package net.lab1024.sa.admin.module.business.product.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品 实体类
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@TableName("product")
public class ProductEntity {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Product Key */
    private String productKey;

    /** Product Secret */
    private String productSecret;

    /** 名称 */
    private String name;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 设备类型 */
    private String deviceType;

    /** 描述 */
    private String description;

    /** 物模型JSON */
    private String modelJson;

    /** 状态 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 是否启用（0禁用/1启用） */
    public boolean isEnabled() {
        return status != null && status != 0;
    }
}
