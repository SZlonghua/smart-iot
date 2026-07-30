package net.lab1024.sa.base.common.message.codec;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;


@Getter
@AllArgsConstructor
public enum DefaultTransport implements Transport {
    MQTT("MQTT"),
    @Deprecated
    MQTT_TLS("MQTT TLS"),
    UDP("UDP"),
    @Deprecated
    UDP_DTLS("UDP DTLS"),
    CoAP("CoAP"),
    @Deprecated
    CoAP_DTLS("CoAP DTLS"),
    TCP("TCP"),
    @Deprecated
    TCP_TLS("TCP TLS"),
    HTTP("HTTP"),
    @Deprecated
    HTTPS("HTTPS"),
    WebSocket("WebSocket"),
    @Deprecated
    WebSockets("WebSocket TLS");

    static {
        Transports.register(Arrays.asList(DefaultTransport.values()));
    }

    private final String name;

    @Override
    public String getId() {
        return name();
    }
}
