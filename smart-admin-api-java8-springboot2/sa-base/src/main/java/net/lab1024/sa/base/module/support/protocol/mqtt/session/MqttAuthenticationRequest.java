package net.lab1024.sa.base.module.support.protocol.mqtt.session;

import lombok.Builder;
import lombok.Data;
import net.lab1024.sa.base.device.AuthenticationRequest;


@Data
@Builder
public class MqttAuthenticationRequest implements AuthenticationRequest {

    private String clientId;
    private String username;
    private String password;
}
