package net.lab1024.sa.admin.module.business.networkcomponent.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 网络组件类型 枚举
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@AllArgsConstructor
@Getter
public enum ComponentTypeEnum implements BaseEnum {

    TCP("tcp", "TCP服务"),

    HTTP("http", "HTTP服务"),

    MQTT_SERVER("mqtt-server", "MQTT服务"),

    MQTT_CLIENT("mqtt-client", "MQTT客户端"),

    ;

    private final String value;

    private final String desc;
}
