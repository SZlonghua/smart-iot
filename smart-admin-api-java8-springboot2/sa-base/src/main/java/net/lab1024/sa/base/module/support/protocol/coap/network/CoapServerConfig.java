package net.lab1024.sa.base.module.support.protocol.coap.network;
import lombok.Getter;
import lombok.Setter;
import net.lab1024.sa.base.common.network.ServerNetworkConfig;
import net.lab1024.sa.base.common.network.NetworkTransport;
@Getter @Setter
public class CoapServerConfig implements ServerNetworkConfig {
    private String id;
    private String host;
    private int port;
    @Override public NetworkTransport getTransport() { return NetworkTransport.UDP; }
    @Override public String getSchema() { return "coap"; }
}
