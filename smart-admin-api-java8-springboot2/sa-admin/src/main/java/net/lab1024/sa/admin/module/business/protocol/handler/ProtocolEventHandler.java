package net.lab1024.sa.admin.module.business.protocol.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.protocol.domain.entity.ProtocolEntity;
import net.lab1024.sa.base.common.event.DeleteAfterEvent;
import net.lab1024.sa.base.common.event.SaveAfterEvent;
import net.lab1024.sa.base.common.event.UpdateAfterEvent;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDefinition;
import net.lab1024.sa.base.common.protocol.ProtocolSupportEvent;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 协议事件处理器 — 监听 ProtocolEntity 的 CRUD 事件，转化为 ProtocolSupportEvent 发布。
 *
 * @Author 廖涛
 * @Date 2026/07/30
 * @Copyright 1024创新实验室
 */
@Slf4j
@Component
public class ProtocolEventHandler {

    @Resource
    private IEventBus eventBus;

    /** 保存 → ProtocolSupportEvent.SAVED */
    @EventListener
    public void onSaved(SaveAfterEvent<ProtocolEntity> event) {
        ProtocolEntity entity = event.getAfterData();
        if (entity == null) return;
        eventBus.publish(ProtocolSupportEvent.of(
                ProtocolSupportEvent.Type.SAVED, entity.toDefinition()));
        log.info("[ProtocolEventHandler] 发布 SAVED 事件 — id={}", entity.getId());
    }

    /** 修改 → ProtocolSupportEvent.UPDATED */
    @EventListener
    public void onUpdated(UpdateAfterEvent<ProtocolEntity> event) {
        ProtocolEntity entity = event.getAfterData();
        if (entity == null) return;
        eventBus.publish(ProtocolSupportEvent.of(
                ProtocolSupportEvent.Type.UPDATED, entity.toDefinition()));
        log.info("[ProtocolEventHandler] 发布 UPDATED 事件 — id={}", entity.getId());
    }

    /** 删除 → ProtocolSupportEvent.DELETED */
    @EventListener
    public void onDeleted(DeleteAfterEvent<ProtocolEntity> event) {
        ProtocolEntity entity = event.getBeforeData();
        eventBus.publish(ProtocolSupportEvent.of(
                ProtocolSupportEvent.Type.DELETED,entity.toDefinition()));
        log.info("[ProtocolEventHandler] 发布 DELETED 事件 — id={}", event.getEntityId());
    }
}
