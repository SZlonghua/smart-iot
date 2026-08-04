package net.lab1024.sa.base.module.support.protocol.tcp.network;
import lombok.Getter;
import lombok.Setter;
import net.lab1024.sa.base.common.network.ClientNetworkConfig;
import net.lab1024.sa.base.common.network.NetworkTransport;
@Getter @Setter
public class TcpClientConfig implements ClientNetworkConfig {
    private String id;
    private String host;
    private int port;
    @Override public NetworkTransport getTransport() { return NetworkTransport.TCP; }
    @Override public String getSchema() { return "tcp"; }
}
