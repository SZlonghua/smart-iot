package net.lab1024.sa.admin.module.business.device.storage.listener;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.device.storage.service.DevicePropertyDataService;
import net.lab1024.sa.base.common.message.ChildDeviceMessage;
import net.lab1024.sa.base.common.message.ReportPropertyMessage;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.storage.model.PropertyData;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 属性上报存储监听器 — 消息 → PropertyData 的完整转换（含设备上下文回填）在本类一次完成。
 * <p>
 * 转换所需的 productId/deviceName 只能来自设备上下文（设备自配置），本类负责解析后转换并全字段填齐，
 * 转换结果直接交数据服务落库（{@link DevicePropertyDataService#store} 只做策略路由 + 持久化，不再回填）。
 * 任何异常仅记日志，绝不阻断消息流。
 * <p>
 * Spring @EventListener 经事件总线桥接收上行消息（publishAsync 已在异步线程执行，本类内 block 不阻塞
 * 消息处理链路）；子设备（网关代理）上报经泛型精准匹配单独入口（ChildDeviceMessage 实现
 * ResolvableTypeProvider），解包后按内层消息处理（headers/deviceId 均取内层）。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/09/07
 * &#064;Copyright  1024创新实验室
 */
@Slf4j
@Component
public class ReportPropertyStorageListener extends CommonStorageListener {

    public ReportPropertyStorageListener(DeviceRegistry deviceRegistry, DevicePropertyDataService propertyDataService) {
        super(deviceRegistry);
        this.propertyDataService = propertyDataService;
    }

    private final DevicePropertyDataService propertyDataService;

    /** 属性上报（设备直连） */
    @EventListener
    public void onReportProperty(ReportPropertyMessage message) {
        handle(message);
    }

    /** 子设备属性上报 — 泛型精准匹配，无需 instanceof */
    @EventListener
    public void onChildDeviceMessage(ChildDeviceMessage<ReportPropertyMessage> wrapper) {
        handle(wrapper.getChildDeviceMessage());
    }

    /** 重复逻辑收敛点 — ignore 跳过 → 设备上下文解析（不存在/未绑定产品跳过）→ 完整转换 → 服务 store */
    private void handle(ReportPropertyMessage message) {
        if (ignoreStorage(message)) {
            return;
        }
        try {
            StoreContext context = resolveStoreContext(message.getDeviceId());
            if (context == null) {
                log.warn("属性上报设备不存在或未绑定产品，跳过存储: deviceId={}", message.getDeviceId());
                return;
            }
            propertyDataService.store(toPropertyData(context, message));
        } catch (Exception e) {
            log.warn("属性上报存储异常，跳过: {}", e.getMessage());
        }
    }

    /** 消息 → PropertyData 完整转换 — productId/deviceName 取设备上下文回填，全字段一次填齐（timestamp 取报文消息时间，落 create_time 列） */
    private PropertyData toPropertyData(StoreContext context, ReportPropertyMessage message) {
        PropertyData data = new PropertyData();
        data.setDeviceId(message.getDeviceId());
        data.setProductId(context.getProduct().getId());
        data.setDeviceName(context.getDeviceName());
        data.setMessageId(message.getMessageId());
        data.setTimestamp(message.getTimestamp());
        data.setProperties(message.getProperties());
        return data;
    }

}
