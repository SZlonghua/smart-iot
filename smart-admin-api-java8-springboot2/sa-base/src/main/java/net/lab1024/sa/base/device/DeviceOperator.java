package net.lab1024.sa.base.device;

import net.lab1024.sa.base.metadata.ThingsMetadata;

public interface DeviceOperator {

    String getDeviceId();

    Long getOnlineTime();

    Long getOfflineTime();

    /**
     * 断开连接
     */
    Boolean disconnect();

    /**
     * 获取设备物模型元数据
     */
    ThingsMetadata getMetadata();


    DeviceProductOperator getProduct();
}
