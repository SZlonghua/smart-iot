package net.lab1024.sa.admin.module.business.device.listener;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.domain.entity.DeviceEntity;
import net.lab1024.sa.base.common.event.DataChangeEvent;
import net.lab1024.sa.base.common.event.DeleteAfterEvent;
import net.lab1024.sa.base.common.event.WriteEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 设备事件监听 — 演示 WriteEvent / DataChangeEvent 包装事件 及异步处理。
 * 泛型 + ResolvableTypeProvider，无需 isEntityType 守卫。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
@Slf4j
@Component
public class DeviceWriteAndAsyncListener {

    @EventListener
    public void onDeviceWrite(WriteEvent<DeviceEntity> event) {
        log.info("[设备写入事件] type={}, id={}, entity={}",
                event.getClass().getSimpleName(),
                event.getEntityId(),
                event.getEntitySimpleName());
    }

    @EventListener
    public void onDeviceChange(DataChangeEvent<DeviceEntity> event) {
        log.info("[设备变更事件] type={}, id={}, table={}",
                event.getClass().getSimpleName(),
                event.getEntityId(),
                event.getTableName());
    }

    @Async("smart-async-executor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeviceDeletedAsync(DeleteAfterEvent<DeviceEntity> event) {
        log.info("[设备删除-异步] id={}, physicalDelete={}, thread={}",
                event.getEntityId(),
                event.isPhysicalDelete(),
                Thread.currentThread().getName());
    }
}
