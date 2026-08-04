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

    TCP("tcp", "TCP服务", "tcp-server"),
//    TCP_CLIENT("tcp_client", "TCP客户端", "tcp-client"),

    MQTT_SERVER("mqtt-server", "MQTT服务", "mqtt-server"),
    MQTT_CLIENT("mqtt-client", "MQTT客户端", "mqtt-client"),

    HTTP("http", "HTTP服务", "http-server"),
//    HTTP_CLIENT("http-client", "HTTP客户端", "http-client"),

//    WS_SERVER("ws-server", "WebSocket服务", "ws-server"),
//    WS_CLIENT("ws-client", "WebSocket客户端", "ws-client"),

    UDP("udp", "UDP", "udp"),

//    COAP_SERVER("coap-server", "CoAP服务", "coap-server"),
//    COAP_CLIENT("coap-client", "CoAP客户端", "coap-client"),

    ;

    private final String value;

    private final String desc;

    /** 对应 NetworkProvider.getId() */
    private final String providerId;
}
