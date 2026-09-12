package net.lab1024.sa.admin.module.business.device.storage.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceProductOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.storage.StorageStrategy;
import net.lab1024.sa.base.storage.model.CommandLogQuery;
import net.lab1024.sa.base.storage.model.MessageLogData;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;

/**
 * 消息日志数据服务 — 命令上下行日志落库 store 与分页查询。
 * <p>
 * store 为纯路由落库入口：消息 → MessageLogData 的完整转换（含设备上下文回填 productId/deviceName）
 * 由监听器完成，本方法仅按产品存储策略路由写入 —— 存储策略经设备操作对象解析
 * （{@link DeviceOperator#getStorageStrategy()}，存储未开启/无匹配策略由 EmptyStorageStrategy 兜底）；
 * store 异常仅记日志，绝不阻断消息流。
 * <p>
 * 分页查询经策略执行（count + 数据两查收敛在策略内），本服务只构造查询条件并透出 PageResult。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Slf4j
@Service
public class MessageLogDataService {

    @Resource
    private DeviceRegistry deviceRegistry;

    /**
     * 消息日志落库 — 按产品存储策略路由写入（监听器在事件总线异步线程调用本方法，block 不阻塞消息处理链路；
     * 设备不存在/未绑定产品或任何异常仅记日志，绝不阻断消息流）
     */
    public void store(MessageLogData data) {
        try {
            requireStorageStrategy(data.getDeviceId()).save(data);
        } catch (Exception e) {
            log.warn("消息日志存储异常，跳过: deviceId={}, {}", data.getDeviceId(), e.getMessage());
        }
    }

    /**
     * 分页查询设备消息日志 — 排序固定时间倒序；commandTypes/direction/deviceName 可空 = 不按该条件过滤
     * （deviceName 为冗余字段精确匹配）；commandTypes 为日志类型多选集合（TopicMessageCodec 枚举名直传，
     * tag IN；回复类型带 Reply 后缀、与命令类型同为独立类型，上下行由 direction 字段区分）
     */
    public PageResult<MessageLogData> queryLog(String deviceId, List<String> commandTypes, String direction, String deviceName,
                                               Long startTime, Long endTime, int pageNum, int pageSize) {
        DeviceProductOperator product = resolveProduct(deviceId);
        CommandLogQuery query = new CommandLogQuery();
        query.setProductId(product.getId());
        query.setDeviceId(deviceId);
        query.setCommandTypes(commandTypes);
        query.setDirection(direction);
        query.setDeviceName(deviceName);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return requireStorageStrategy(deviceId).queryCommandLogHistory(query);
    }

    /**
     * 设备 → 消息数据存储策略 — 经设备操作对象按所属产品存储策略解析（block 风格与 DeviceService 一致）；
     * 策略恒非空：设备未绑定产品/产品不存在、存储未开启/无匹配策略一律由 EmptyStorageStrategy 兜底
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
