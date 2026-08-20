package net.lab1024.sa.admin.module.business.protocol.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.gateway.service.GatewayService;
import net.lab1024.sa.admin.module.business.protocol.domain.entity.ProtocolEntity;
import net.lab1024.sa.base.common.event.DeleteAfterEvent;
import net.lab1024.sa.base.common.event.DeleteBeforeEvent;
import net.lab1024.sa.base.common.event.SaveAfterEvent;
import net.lab1024.sa.base.common.event.UpdateAfterEvent;
import net.lab1024.sa.base.common.exception.BusinessException;
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

    @Resource
    private GatewayService gatewayService;

    /** 删除前 → 校验网关引用 */
    @EventListener
    public void onBeforeDelete(DeleteBeforeEvent<ProtocolEntity> event) {
        long refCount = gatewayService.countByProtocolId(event.getEntityId());
        if (refCount > 0) throw new BusinessException("该协议被 " + refCount + " 个网关引用，无法删除");
    }

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
