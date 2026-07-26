package net.lab1024.sa.admin.module.business.device.registry.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.registry.event.DeviceConfigsProductChangeEvent;
import net.lab1024.sa.base.common.util.SmartMapUtil;
import net.lab1024.sa.base.device.DeviceProductOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.support.DeviceField;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 产品变更同步处理 — 监听 {@link DeviceConfigsProductChangeEvent}，
 * 异步更新该产品下所有设备的冗余产品字段（productName / productKey / productSecret）。
 *
 * @Author 廖涛
 * @Date 2026/07/25
 * @Copyright 1024创新实验室
 */
@Slf4j
@Component
public class ProductChangeSyncHandler {

    @Resource
    private DeviceRegistry deviceRegistry;

    /** 产品信息变更时，同步更新该产品下所有设备的冗余字段 */
    @Async("smart-async-executor")
    @EventListener
    public void onProductChange(DeviceConfigsProductChangeEvent event) {
        String productId = String.valueOf(event.getProductId());

        deviceRegistry.getProduct(productId)
                .flatMapMany(DeviceProductOperator::getDevices)
                .doOnNext(device -> device.setConfigs(buildProductSyncMap(event)))
                .count()
                .subscribe(
                        cnt -> log.info("[ProductChangeSync] 已同步 {} 个设备的冗余产品字段 — productId={}",
                                cnt, productId),
                        err -> log.error("[ProductChangeSync] 同步失败 — productId={}", productId, err)
                );
    }

    private static Map<String, Object> buildProductSyncMap(DeviceConfigsProductChangeEvent event) {
        return SmartMapUtil.builder()
                .put(DeviceField.PRODUCT_NAME.getValue(), event.getProductName())
                .put(DeviceField.PRODUCT_KEY.getValue(), event.getProductKey())
                .put(DeviceField.PRODUCT_SECRET.getValue(), event.getProductSecret())
                .build();
    }
}
