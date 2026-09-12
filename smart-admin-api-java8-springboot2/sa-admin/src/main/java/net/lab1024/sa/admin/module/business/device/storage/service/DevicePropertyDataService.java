package net.lab1024.sa.admin.module.business.device.storage.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceProductOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.storage.StorageStrategy;
import net.lab1024.sa.base.storage.model.LatestPropertyQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyValue;
import net.lab1024.sa.base.storage.model.PropertyData;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;

/**
 * 设备属性数据服务 — 属性上报落库 store 与最新属性查询。
 * <p>
 * store 为纯路由落库入口：消息 → PropertyData 的完整转换（含设备上下文回填 productId/deviceName）
 * 由监听器完成，本方法仅按产品存储策略路由写入 —— 存储策略经设备操作对象解析
 * （{@link DeviceOperator#getStorageStrategy()}，存储未开启/无匹配策略由 EmptyStorageStrategy 兜底）；
 * store 异常仅记日志，绝不阻断消息流。
 * <p>
 * 最新值查询经策略执行业务语义（策略按自身行形态取数/解码），本服务只构造查询条件
 * （productId 需先经设备上下文解析）并透出结果，不感知具体存储库与行布局。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Slf4j
@Service
public class DevicePropertyDataService {

    @Resource
    private DeviceRegistry deviceRegistry;

    /**
     * 属性上报落库 — 按产品存储策略路由写入（监听器在事件总线异步线程调用本方法，block 不阻塞消息处理链路；
     * 设备不存在/未绑定产品或任何异常仅记日志，绝不阻断消息流）
     */
    public void store(PropertyData data) {
        try {
            requireStorageStrategy(data.getDeviceId())
                    .save(data);
        } catch (Exception e) {
            log.warn("属性上报存储异常，跳过: deviceId={}, {}", data.getDeviceId(), e.getMessage());
        }
    }

    /**
     * 批量查询设备最新上报属性值 — 每个属性独立取最后一条非空上报记录（值 + 上报时间毫秒；策略按自身行形态
     * 取数：row 为该属性列 ts 倒序首行、column 为单值行 5 类型值列首个非空）；属性从未上报 → 结果不含该属性；
     * 存储未开启或无匹配策略 → EmptyStorageStrategy 兜底查询明确抛 BusinessException
     */
    public Map<String, LatestPropertyValue> queryLatestPropertyValues(String deviceId, Collection<String> propertyIds) {
        DeviceProductOperator product = resolveProduct(deviceId);
        LatestPropertyQuery query = new LatestPropertyQuery();
        query.setProductId(product.getId());
        query.setDeviceId(deviceId);
        query.setPropertyIds(propertyIds);
        return requireStorageStrategy(deviceId)
                .queryLatestPropertyValues(query);
    }

    /**
     * 设备 → 消息数据存储策略 — 经设备操作对象按所属产品存储策略解析（block 风格与 DeviceService 一致）；
     * 设备不存在或未绑定产品 → 抛异常（策略恒非空：存储未开启/无匹配策略由 EmptyStorageStrategy 兜底）
     */
    private StorageStrategy requireStorageStrategy(String deviceId) {
        return deviceRegistry.getDevice(deviceId)
                .flatMap(DeviceOperator::getStorageStrategy)
                .block();
    }

    /** 设备 → 产品操作对象（block 风格与 DeviceService 一致；设备不存在或未绑定产品抛异常） */
    private DeviceProductOperator resolveProduct(String deviceId) {
        return deviceRegistry.getDevice(deviceId)
                .flatMap(DeviceOperator::getProduct)
                .switchIfEmpty(Mono.error(new BusinessException("设备不存在或未绑定产品")))
                .block();
    }
}
