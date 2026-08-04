package net.lab1024.sa.base.module.support.protocol.http.network;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.network.DefaultNetworkType;
import net.lab1024.sa.base.common.network.NetworkType;
@Slf4j
public class VertxHttpClientNetwork implements HttpClientNetwork {
    @Override public String getId() { return null; }
    @Override public NetworkType getType() { return DefaultNetworkType.HTTP_CLIENT; }
    @Override public void shutdown() { log.info("[VertxHttpClient] shutdown"); }
    @Override public boolean isAlive() { return true; }
    @Override public boolean isAutoReload() { return true; }
}
