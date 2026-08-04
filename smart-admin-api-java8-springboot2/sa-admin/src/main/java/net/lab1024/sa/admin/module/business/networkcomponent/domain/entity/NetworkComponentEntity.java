package net.lab1024.sa.admin.module.business.networkcomponent.domain.entity;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.admin.module.business.networkcomponent.constant.ComponentTypeEnum;
import net.lab1024.sa.base.common.network.NetworkProperties;
import net.lab1024.sa.base.common.util.SmartEnumUtil;

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

    public boolean isEnabled() {
        return status == null || status != 0;
    }

    public NetworkProperties toProperties() {
        NetworkProperties p = new NetworkProperties();
        p.setId(String.valueOf(id));
        p.setName(name);
        p.setType(getNetworkProviderId());
//        p.setType(type);
        p.setEnabled(status != null && status == 1);
        p.setConfigurations(JSON.parseObject(configuration, java.util.Map.class));
        return p;
    }

    private String getNetworkProviderId() {
        return SmartEnumUtil.getEnumByValue(type, ComponentTypeEnum.class).getProviderId();
    }
}
