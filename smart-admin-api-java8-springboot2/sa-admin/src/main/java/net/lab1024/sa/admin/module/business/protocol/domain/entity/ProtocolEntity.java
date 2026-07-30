package net.lab1024.sa.admin.module.business.protocol.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协议 实体类
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@TableName("protocol")
public class ProtocolEntity {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 名称 */
    private String name;

    /** 版本号 */
    private String version;

    /** 加载方式：jar / local */
    private String loader;

    /** JAR包路径 */
    private String jarPath;

    /** JAR包原始文件名 */
    private String jarName;

    /** 描述 */
    private String description;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public net.lab1024.sa.base.common.protocol.ProtocolSupportDefinition toDefinition() {
        java.util.Map<String, Object> configuration = new java.util.HashMap<>();
        configuration.put("jarPath", jarPath);
        configuration.put("jarName", jarName);

        return net.lab1024.sa.base.common.protocol.ProtocolSupportDefinition.builder()
                .id(String.valueOf(id))
                .name(name)
                .description(description)
                .loader(loader)
                .configuration(configuration)
                .build();
    }
}
