package net.lab1024.sa.base.module.support.protocol.tcp.network;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.network.DefaultNetworkType;
import net.lab1024.sa.base.common.network.NetworkType;

@Slf4j
public class VertxTcpServerNetwork implements TcpServerNetwork {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public NetworkType getType() {
        return DefaultNetworkType.TCP_SERVER;
    }

    @Override
    public void shutdown() {
        log.info("[VertxTcpServer] shutdown");
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public boolean isAutoReload() {
        return true;
    }
}
