package net.lab1024.sa.base.common.network;

public interface NetworkConfig {

    /**
     * @return 获取配置ID
     */
    String getId();

    /**
     *
     * @return 网络协议类型 TCP or UDP
     */
    NetworkTransport getTransport();

    /**
     * 传输模式,如: http,mqtt,ws
     * @return 传输模式
     */
    String getSchema();
}
