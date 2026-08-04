package net.lab1024.sa.base.module.support.protocol.coap.network;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.network.DefaultNetworkType;
import net.lab1024.sa.base.common.network.NetworkType;
@Slf4j
public class VertxCoapServerNetwork implements CoapServerNetwork {
    @Override public String getId() { return null; }
    @Override public NetworkType getType() { return DefaultNetworkType.COAP_SERVER; }
    @Override public void shutdown() { log.info("[VertxCoapServer] shutdown"); }
    @Override public boolean isAlive() { return true; }
    @Override public boolean isAutoReload() { return true; }
}
