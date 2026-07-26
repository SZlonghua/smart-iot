package net.lab1024.sa.admin.module.business.device.registry.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.domain.entity.DeviceEntity;
import net.lab1024.sa.base.common.event.DeleteAfterEvent;
import net.lab1024.sa.base.common.event.SaveAfterEvent;
import net.lab1024.sa.base.common.event.UpdateAfterEvent;
import net.lab1024.sa.base.device.DeviceInfo;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.support.ProductField;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * 设备 CRUD 事件处理 — 同步设备信息到 Redis 设备注册中心。
 * 设备启用时注册到 Redis（包含 Hash + Set 绑定 + mapping 映射），禁用或删除时注销。
 *
 * @Author 廖涛
 * @Date 2026/07/25
 * @Copyright 1024创新实验室
 */
@Slf4j
@Component
public class DeviceConfigsHandler {

    @Resource
    private DeviceRegistry deviceRegistry;

    /** 设备新增后 — 注册到 Redis */
    @EventListener
    public void onDeviceSaved(SaveAfterEvent<DeviceEntity> event) {
        DeviceEntity device = event.getAfterData();
        if (device == null) {
            return;
        }
        deviceRegistry.register(toDeviceInfo(device))
                .doOnNext(op -> log.info("[DeviceConfigs] 设备已注册到 Redis — deviceId={}, deviceName={}",
                        device.getId(), device.getName()))
                .subscribe();
    }

    /** 设备修改后 — 更新 Redis */
    @EventListener
    public void onDeviceUpdated(UpdateAfterEvent<DeviceEntity> event) {
        DeviceEntity device = event.getAfterData();
        if (device == null) {
            return;
        }
        deviceRegistry.register(toDeviceInfo(device))
                .doOnNext(op -> log.info("[DeviceConfigs] 设备已更新 Redis — deviceId={}, deviceName={}",
                        device.getId(), device.getName()))
                .subscribe();
    }

    /** 设备删除后 — 注销 Redis */
    @EventListener
    public void onDeviceDeleted(DeleteAfterEvent<DeviceEntity> event) {
        String deviceId = String.valueOf(event.getEntityId());
        deviceRegistry.unregisterDevice(deviceId)
                .doOnSuccess(v -> log.info("[DeviceConfigs] 设备删除，已注销 Redis — deviceId={}", deviceId))
                .subscribe();
    }

    /** DeviceEntity → DeviceInfo，产品冗余字段从注册中心获取 */
    private DeviceInfo toDeviceInfo(DeviceEntity entity) {
        return Mono.justOrEmpty(entity.getProductId())
                .flatMap(id -> deviceRegistry.getProduct(String.valueOf(id)))
                .flatMap(p -> p.getSelfConfigValues(
                        ProductField.PRODUCT_KEY.getValue(),
                        ProductField.PRODUCT_SECRET.getValue()
                ))
                .map(values -> buildInfo(entity, values.get(0).asString(), values.get(1).asString()))
                .switchIfEmpty(Mono.fromCallable(() -> buildInfo(entity, null, null)))
                .block();
    }

    private DeviceInfo buildInfo(DeviceEntity entity, String productKey, String productSecret) {
        return DeviceInfo.builder()
                .deviceId(String.valueOf(entity.getId()))
                .deviceName(entity.getName())
                .deviceKey(entity.getDeviceKey())
                .deviceSecret(entity.getDeviceSecret())
                .productId(String.valueOf(entity.getProductId()))
                .productName(entity.getProductName())
                .productKey(productKey)
                .productSecret(productSecret)
                .build();
    }
}
