package net.lab1024.sa.base.module.support.protocol.websocket.network;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.network.DefaultNetworkType;
import net.lab1024.sa.base.common.network.NetworkType;
@Slf4j
public class VertxWebSocketServerNetwork implements WebSocketServerNetwork {
    @Override public String getId() { return null; }
    @Override public NetworkType getType() { return DefaultNetworkType.WEB_SOCKET_SERVER; }
    @Override public void start() { log.info("[VertxWebSocketServer] start"); }
    @Override public void shutdown() { log.info("[VertxWebSocketServer] shutdown"); }
    @Override public boolean isAlive() { return true; }
    @Override public boolean isAutoReload() { return true; }
}
