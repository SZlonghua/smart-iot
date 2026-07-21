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
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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

    /** 读取设备属性 — 下发命令到设备获取 */
    public Map<String, Object> readProperties(String deviceId, List<String> properties) {
        // TODO: 后续对接设备网关下发读取属性指令
        return new HashMap<String, Object>();
    }

    /** 查询设备属性 — 查数据库 */
    public List<DevicePropertyVO> getProperties(Long deviceId, List<String> properties) {
        // TODO: 后续查询 device_property 表，按 property IDs 过滤
        return new ArrayList<DevicePropertyVO>();
    }

    /** 设置设备属性 — 下发命令到设备 */
    public Map<String, Object> writeProperties(String deviceId, Map<String, Object> properties) {
        // TODO: 后续对接设备网关下发设置属性指令
        return properties;
    }

    /** 调用设备功能 — 下发命令到设备 */
    public Object invokeFunction(String deviceId, String functionId, Map<String, Object> properties) {
        // TODO: 后续对接设备网关下发功能调用指令
        return new HashMap<String, Object>();
    }

    private String generateDeviceKey() {
        return "DK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String generateDeviceSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
