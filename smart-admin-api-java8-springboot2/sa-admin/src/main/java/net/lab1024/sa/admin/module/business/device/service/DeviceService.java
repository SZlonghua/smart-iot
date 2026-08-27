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
import net.lab1024.sa.admin.module.business.gateway.service.GatewayService;
import net.lab1024.sa.admin.module.business.product.service.ProductService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.device.DeviceSendOperator;
import net.lab1024.sa.base.device.support.DeviceOfflineCleaner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
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

    /** 查询设备属性 — 查数据库 */
    public List<DevicePropertyVO> getProperties(Long deviceId, List<String> properties) {
        // TODO: 后续查询 device_property 表，按 property IDs 过滤
        return new ArrayList<DevicePropertyVO>();
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
