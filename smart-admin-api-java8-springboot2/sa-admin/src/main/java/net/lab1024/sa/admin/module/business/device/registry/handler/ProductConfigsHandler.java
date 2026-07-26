package net.lab1024.sa.admin.module.business.device.registry.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.registry.event.DeviceConfigsProductChangeEvent;
import net.lab1024.sa.admin.module.business.product.constant.DeviceTypeEnum;
import net.lab1024.sa.admin.module.business.product.domain.entity.ProductEntity;
import net.lab1024.sa.base.common.event.DeleteAfterEvent;
import net.lab1024.sa.base.common.event.SaveAfterEvent;
import net.lab1024.sa.base.common.event.UpdateAfterEvent;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.ProductInfo;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 产品 CRUD 事件处理 — 同步产品信息到 Redis 设备注册中心。
 * 产品启用时注册到 Redis，禁用或删除时注销。
 * 产品信息变更时发布 {@link DeviceConfigsProductChangeEvent} 通知设备同步冗余字段。
 *
 * @Author 廖涛
 * @Date 2026/07/25
 * @Copyright 1024创新实验室
 */
@Slf4j
@Component
public class ProductConfigsHandler {

    @Resource
    private DeviceRegistry deviceRegistry;

    @Resource
    private IEventBus eventBus;

    /** 产品新增后 — 启用则注册到 Redis */
    @EventListener
    public void onProductSaved(SaveAfterEvent<ProductEntity> event) {
        ProductEntity product = event.getAfterData();
        if (product != null && product.isEnabled()) {
            registerAndNotify(product);
        }
    }

    /** 产品修改后 — 启用则注册，禁用则注销 */
    @EventListener
    public void onProductUpdated(UpdateAfterEvent<ProductEntity> event) {
        ProductEntity product = event.getAfterData();
        if (product == null) {
            return;
        }
        if (product.isEnabled()) {
            registerAndNotify(product);
        } else {
            String productId = String.valueOf(product.getId());
            deviceRegistry.unregisterProduct(productId)
                    .doOnSuccess(v -> log.info("[ProductConfigs] 产品禁用，已注销 Redis — productId={}", productId))
                    .subscribe();
        }
    }

    /** 产品删除后 — 注销 Redis */
    @EventListener
    public void onProductDeleted(DeleteAfterEvent<ProductEntity> event) {
        String productId = String.valueOf(event.getEntityId());
        deviceRegistry.unregisterProduct(productId)
                .doOnSuccess(v -> log.info("[ProductConfigs] 产品删除，已注销 Redis — productId={}", productId))
                .subscribe();
    }

    /** 注册产品到 Redis 并发布变更事件 */
    private void registerAndNotify(ProductEntity product) {
        deviceRegistry.register(toProductInfo(product))
                .doOnNext(op -> {
                    log.info("[ProductConfigs] 产品已注册到 Redis — productId={}, productName={}",
                            product.getId(), product.getName());
                    eventBus.publish(new DeviceConfigsProductChangeEvent(
                            product.getId(), product.getName(),
                            product.getProductKey(), product.getProductSecret()));
                })
                .subscribe();
    }

    /** ProductEntity → ProductInfo */
    private ProductInfo toProductInfo(ProductEntity entity) {
        return ProductInfo.builder()
                .productId(String.valueOf(entity.getId()))
                .productName(entity.getName())
                .metadata(entity.getModelJson())
                .productKey(entity.getProductKey())
                .productSecret(entity.getProductSecret())
                .isGatewayDevice(DeviceTypeEnum.GATEWAY.getValue().equals(entity.getDeviceType()))
                .build();
    }
}
