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

    MQTT_BROKER("mqtt_broker", "MQTT Broker"),

    HTTP_SERVER("http_server", "HTTP Server"),

    COAP_SERVER("coap_server", "CoAP Server"),

    TCP_SERVER("tcp_server", "TCP Server"),

    ;

    private final String value;

    private final String desc;
}
