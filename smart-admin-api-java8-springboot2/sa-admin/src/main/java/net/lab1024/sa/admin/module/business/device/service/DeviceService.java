package net.lab1024.sa.admin.module.business.device.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.device.dao.DeviceDao;
import net.lab1024.sa.admin.module.business.device.domain.entity.DeviceEntity;
import net.lab1024.sa.admin.module.business.device.domain.form.DeviceAddForm;
import net.lab1024.sa.admin.module.business.device.domain.form.DeviceQueryForm;
import net.lab1024.sa.admin.module.business.device.domain.form.DeviceUpdateForm;
import net.lab1024.sa.admin.module.business.device.domain.vo.DeviceVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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

    /**
     * 分页查询
     */
    public PageResult<DeviceVO> queryPage(DeviceQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<DeviceVO> list = deviceDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(DeviceAddForm addForm) {
        DeviceEntity entity = SmartBeanUtil.copy(addForm, DeviceEntity.class);
        entity.setDeviceKey(generateDeviceKey());
        entity.setDeviceSecret(generateDeviceSecret());
        entity.setStatus(0);
        deviceDao.insert(entity);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     */
    public ResponseDTO<String> update(DeviceUpdateForm updateForm) {
        DeviceEntity entity = SmartBeanUtil.copy(updateForm, DeviceEntity.class);
        deviceDao.updateById(entity);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ResponseDTO.ok();
        }
        deviceDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(Long id) {
        if (null == id) {
            return ResponseDTO.ok();
        }
        deviceDao.deleteById(id);
        return ResponseDTO.ok();
    }

    private String generateDeviceKey() {
        return "DK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String generateDeviceSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
