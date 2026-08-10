package net.lab1024.sa.base.common.network;

public interface Network {
    /**
     * ID唯一标识
     *
     * @return ID
     */
    String getId();

    /**
     * @return 网络类型
     * @see DefaultNetworkType
     */
    NetworkType getType();

    /**
     * 启动网络组件
     */
    void start();

    /**
     * 关闭网络组件
     */
    void shutdown();

    /**
     * @return 是否存活
     */
    boolean isAlive();

    /**
     * 当{@link Network#isAlive()}为false是,是否自动重新加载.
     *
     * @return 是否重新加载
     */
    boolean isAutoReload();
}
