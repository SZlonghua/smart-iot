package net.lab1024.sa.base.module.support.protocol.udp.network;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.network.DefaultNetworkType;
import net.lab1024.sa.base.common.network.NetworkType;
@Slf4j
public class VertxUdpNetwork implements UdpNetwork {
    @Override public String getId() { return null; }
    @Override public NetworkType getType() { return DefaultNetworkType.UDP; }
    @Override public void start() { log.info("[VertxUdp] start"); }
    @Override public void shutdown() { log.info("[VertxUdp] shutdown"); }
    @Override public boolean isAlive() { return true; }
    @Override public boolean isAutoReload() { return true; }
}
