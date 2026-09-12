package net.lab1024.sa.admin.module.business.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.device.dao.DeviceDao;
import net.lab1024.sa.admin.module.business.device.domain.entity.DeviceEntity;
import net.lab1024.sa.admin.module.business.device.domain.form.DeviceAddForm;
import net.lab1024.sa.admin.module.business.device.domain.form.DeviceQueryForm;
import net.lab1024.sa.admin.module.business.device.domain.form.DeviceUpdateForm;
import net.lab1024.sa.admin.module.business.device.domain.vo.DeviceDetailVO;
import net.lab1024.sa.admin.module.business.device.domain.vo.DevicePropertyVO;
import net.lab1024.sa.admin.module.business.device.domain.vo.DeviceVO;
import net.lab1024.sa.admin.module.business.device.storage.service.DevicePropertyDataService;
import net.lab1024.sa.admin.module.business.gateway.service.GatewayService;
import net.lab1024.sa.admin.module.business.product.domain.vo.ProductDetailVO;
import net.lab1024.sa.admin.module.business.product.service.ProductService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.storage.model.LatestPropertyValue;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartLocalDateUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.device.DeviceSendOperator;
import net.lab1024.sa.base.device.support.DeviceOfflineCleaner;
import net.lab1024.sa.base.metadata.PropertyMetadata;
import net.lab1024.sa.base.metadata.ThingsMetadata;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 设备 Service
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class DeviceService {

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private ProductService productService;

    @Resource
    private GatewayService gatewayService;

    @Resource
    private DeviceSendOperator deviceSendOperator;

    @Resource
    private DeviceOfflineCleaner offlineCleaner;

    @Resource
    private DevicePropertyDataService propertyDataService;
    @Autowired
    private DeviceRegistry deviceRegistry;

    /** 分页查询 */
    public PageResult<DeviceVO> queryPage(DeviceQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<DeviceVO> list = deviceDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /** 添加 */
    public ResponseDTO<String> add(DeviceAddForm addForm) {
        DeviceEntity entity = SmartBeanUtil.copy(addForm, DeviceEntity.class);
        entity.setDeviceKey(generateDeviceKey());
        entity.setDeviceSecret(generateDeviceSecret());
        entity.setStatus(0);
        deviceDao.insert(entity);
        return ResponseDTO.ok();
    }

    /** 更新 */
    public ResponseDTO<String> update(DeviceUpdateForm updateForm) {
        DeviceEntity entity = SmartBeanUtil.copy(updateForm, DeviceEntity.class);
        deviceDao.updateById(entity);
        return ResponseDTO.ok();
    }

    /** 批量删除 */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ResponseDTO.ok();
        }
        deviceDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /** 单个删除 */
    public ResponseDTO<String> delete(Long id) {
        if (null == id) {
            return ResponseDTO.ok();
        }
        deviceDao.deleteById(id);
        return ResponseDTO.ok();
    }

    /** 设备详情 */
    public DeviceDetailVO getDetail(Long id) {
        DeviceEntity device = deviceDao.selectById(id);
        if (device == null) {
            return null;
        }
        // 接触即校验 — 校验在线状态；离线（kill -9/断电等异常残留）→ 清除会话残留（Redis/DB）后重查，返回修正后的状态
        offlineCleaner.checkAndClean(String.valueOf(id)).block();
        device = deviceDao.selectById(id);

        DeviceDetailVO vo = SmartBeanUtil.copy(device, DeviceDetailVO.class);

        // 产品详情
        if(device.getProductId() != null) {
            vo.setProductDetail(productService.getDetail(device.getProductId()));
        }

        // 父设备
        if (device.getParentDeviceId() != null) {
            vo.setParentDevice(getById(device.getParentDeviceId()));
        }
        // 设备网关详情
        if (device.getGatewayId() != null) {
            vo.setGatewayDetail(gatewayService.getDetail(device.getGatewayId()));
        }
        return vo;
    }

    private DeviceVO getById(Long id) {
        DeviceEntity device = deviceDao.selectById(id);
        if (device == null) {
            return null;
        }
        return SmartBeanUtil.copy(device, DeviceVO.class);
    }

    /** 读取设备属性 — 同步下发，等待设备回复（默认 10s），返回属性值（失败抛 BusinessException，成功但数据为空返回 null） */
    public Map<String, Object> readProperties(String deviceId, List<String> properties) {
        return deviceSendOperator.readProperty()
                .send(deviceId, properties)
                .flatMap(reply -> reply.isSuccess()
                        ? Mono.justOrEmpty(reply.getProperties())
                        : Mono.error(replyError(reply.getMessage())))
                .block();
    }

    /**
     * 查询设备属性（最近上报值）— 每个属性独立取消息数据存储中最后一条非空上报记录。
     * 属性列表为空 = 产品物模型全部属性；未上报过（列/表不存在或存储无记录）→ 值与时间为空不报错。
     * 存储未开启/无匹配策略 → EmptyStorageStrategy 兜底明确报错
     */
    public List<DevicePropertyVO> getProperties(Long deviceId, List<String> properties) {
        // 一镜到底：设备 → 物模型 → 属性定义解析（Flux）→ 定义列表 → 最新值查询 + 逐条组装 VO（见 propertyVos），链末统一 block；
        // 设备未注册/物模型缺失/属性全未命中 → 空流由 defaultIfEmpty 兜底为空列表
        return deviceRegistry.getDevice(String.valueOf(deviceId))
                .flatMap(DeviceOperator::getMetadata)
                .flatMapMany(metadata -> resolveDefinitions(metadata, properties))
                .collectList()
                .flatMapMany(definitions -> propertyVos(deviceId, definitions))
                .collectList()
                .defaultIfEmpty(new ArrayList<>())
                .block();
    }

    /** 定义列表 → 属性 VO 流（Flux）— 批量取各属性最新上报值后交 assembleVos 按定义顺序组装 */
    private Flux<DevicePropertyVO> propertyVos(Long deviceId, List<PropertyMetadata> definitions) {
        return latestValues(deviceId, definitions)
                .flatMapMany(latestMap -> assembleVos(definitions, latestMap));
    }

    /** 定义列表 × 最新上报值 Map → 属性 VO 流（Flux）— 按定义顺序逐条组装；未上报属性经 propertyVo 保留空值行 */
    private static Flux<DevicePropertyVO> assembleVos(List<PropertyMetadata> definitions,
                                                      Map<String, LatestPropertyValue> latestMap) {
        return Flux.fromIterable(definitions)
                .map(definition -> propertyVo(definition, latestMap.get(definition.getId())));
    }

    /** 定义列表 → 最新上报值 Map（Mono）— 定义 id 提取（Flux）后批量查询；同步 DAO 经 fromSupplier 挂载为异步源 */
    private Mono<Map<String, LatestPropertyValue>> latestValues(Long deviceId, List<PropertyMetadata> definitions) {
        return Flux.fromIterable(definitions)
                .map(PropertyMetadata::getId)
                .collectList()
                .flatMap(propertyIds -> Mono.fromSupplier(() ->
                        propertyDataService.queryLatestPropertyValues(String.valueOf(deviceId), propertyIds)));
    }

    /** 单条属性定义 + 最新值 → VO — latest 为空（从未上报）时值/时间为空，前端显示无数据 */
    private static DevicePropertyVO propertyVo(PropertyMetadata definition, LatestPropertyValue latest) {
        DevicePropertyVO vo = new DevicePropertyVO();
        vo.setPropertyId(definition.getId());
        vo.setPropertyName(definition.getName());
        if (latest != null) {
            vo.setFormatValue(String.valueOf(latest.getValue()));
            vo.setTimeValue(SmartLocalDateUtil.toLocalDateTime(latest.getTimestamp()));
        }
        return vo;
    }

    /**
     * 属性定义解析（Flux）— 入参为空 → 物模型全部属性；非空 → 按入参顺序取定义，物模型不存在的属性跳过。
     * 顺序保真用 concatMap 逐项串行解析，出流顺序与入参一致
     */
    private Flux<PropertyMetadata> resolveDefinitions(ThingsMetadata metadata, List<String> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return Flux.fromIterable(metadata.getProperties());
        }
        return Flux.fromIterable(properties)
                .concatMap(propertyId -> Mono.justOrEmpty(metadata.getPropertyOrNull(propertyId)));
    }

    /** 设置设备属性 — 同步下发，等待设备回复（默认 10s），返回最新属性值（失败抛 BusinessException，成功但数据为空返回 null） */
    public Map<String, Object> writeProperties(String deviceId, Map<String, Object> properties) {
        return deviceSendOperator.writeProperty()
                .send(deviceId, properties)
                .flatMap(reply -> reply.isSuccess()
                        ? Mono.justOrEmpty(reply.getProperties())
                        : Mono.error(replyError(reply.getMessage())))
                .block();
    }

    /** 调用设备功能 — 同步下发，等待设备回复（默认 10s；物模型异步功能下发即成功），返回输出结果（失败抛 BusinessException，成功但无输出返回 null） */
    public Object invokeFunction(String deviceId, String functionId, Map<String, Object> properties) {
        return deviceSendOperator.invokeFunction()
                .send(deviceId, functionId, properties)
                .flatMap(reply -> reply.isSuccess()
                        ? Mono.justOrEmpty(reply.getOutput())
                        : Mono.error(replyError(reply.getMessage())))
                .block();
    }

    /** 失败回复 → BusinessException — message 为空时兜底固定文案（null 会让前端 message.error 不渲染，出现"无提示"） */
    private BusinessException replyError(String message) {
        return StringUtils.isBlank(message)
                ? new BusinessException("设备执行失败，请检查设备回复")
                : new BusinessException(message);
    }

    private String generateDeviceKey() {
        return "DK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String generateDeviceSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
