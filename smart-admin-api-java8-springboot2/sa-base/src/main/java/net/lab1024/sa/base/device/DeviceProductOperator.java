package net.lab1024.sa.base.device;

import net.lab1024.sa.base.metadata.ThingsMetadata;

import java.util.List;

public interface DeviceProductOperator {

    String getId();

    ThingsMetadata getMetadata();

    List<DeviceOperator> getDevices();
}
