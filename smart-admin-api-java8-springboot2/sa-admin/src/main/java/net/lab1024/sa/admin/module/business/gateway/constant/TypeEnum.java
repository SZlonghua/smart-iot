package net.lab1024.sa.admin.module.business.gateway.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 设备网关类型 枚举
 *
 * @Author 廖涛
 * @Date 2026/06/01
 * @Copyright 1024创新实验室
 */
@AllArgsConstructor
@Getter
public enum TypeEnum implements BaseEnum {

    MQTT_DIRECT("mqtt_direct", "MQTT直接连接"),

    GATEWAY_CHILD("gateway_child", "网关子设备接入"),

    HTTP_PUSH("http_push", "HTTP推送接入"),

    MQTT_BROKER("mqtt_broker", "MQTT Broker接入"),

    TCP_TRANSPARENT("tcp_transparent", "TCP透传接入"),

    ;

    private final String value;

    private final String desc;
}
