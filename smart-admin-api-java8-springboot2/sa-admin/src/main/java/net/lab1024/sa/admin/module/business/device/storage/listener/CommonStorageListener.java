package net.lab1024.sa.admin.module.business.device.storage.listener;

import lombok.Getter;
import net.lab1024.sa.base.common.message.Headers;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceProductOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.support.DeviceField;
import net.lab1024.sa.base.module.support.cache.core.Value;

public class CommonStorageListener {

    private final DeviceRegistry deviceRegistry;

    public CommonStorageListener(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    public static boolean ignoreStorage(Message message) {
        return Boolean.TRUE.equals(message.getHeaderOrElse(Headers.IGNORE_STORAGE.getValue(), null));
    }

    /** 落库上下文解析（block：监听器在事件总线异步线程调用）— 设备不存在/未绑定产品 → null（监听器跳过降级）；设备名缺失 → deviceName 为 null（存储层字段缺省，不影响落库） */
    public StoreContext resolveStoreContext(String deviceId) {
        if (deviceId == null) {
            return null;
        }
        return deviceRegistry.getDevice(deviceId)
                .flatMap(device -> device.getProduct()
                        .map(product -> new StoreContext(product, deviceName(device))))
                .block();
    }

    /** 设备名称冗余字段 — 设备自配置缺失 → null（存储层字段缺省）；Reactor 不接受 null 发射，故此处同步取值 */
    private String deviceName(DeviceOperator device) {
        return device.getSelfConfig(DeviceField.DEVICE_NAME.getValue())
                .blockOptional()
                .map(Value::asString)
                .orElse(null);
    }

    /** 设备存储上下文（产品操作对象 + 冗余设备名） */
    @Getter
    public static class StoreContext {

        private final DeviceProductOperator product;
        private final String deviceName;

        StoreContext(DeviceProductOperator product, String deviceName) {
            this.product = product;
            this.deviceName = deviceName;
        }

    }
}
