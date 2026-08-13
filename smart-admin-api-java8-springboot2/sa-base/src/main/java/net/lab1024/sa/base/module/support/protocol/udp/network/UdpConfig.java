package net.lab1024.sa.base.module.support.protocol.udp.network;
import lombok.Getter;
import lombok.Setter;
import net.lab1024.sa.base.common.network.NetworkConfig;
import net.lab1024.sa.base.common.network.NetworkTransport;
@Getter @Setter
public class UdpConfig implements NetworkConfig {
    private String id;
    private String host;
    private int port;
    @Override public NetworkTransport getTransport() { return NetworkTransport.UDP; }
    @Override public String getSchema() { return "udp"; }
}
