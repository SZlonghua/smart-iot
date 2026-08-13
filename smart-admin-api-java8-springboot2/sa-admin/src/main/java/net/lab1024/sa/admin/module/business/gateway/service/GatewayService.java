package net.lab1024.sa.admin.module.business.gateway.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.gateway.dao.GatewayDao;
import net.lab1024.sa.admin.module.business.gateway.domain.entity.GatewayEntity;
import net.lab1024.sa.admin.module.business.gateway.domain.form.GatewayAddForm;
import net.lab1024.sa.admin.module.business.gateway.domain.form.GatewayQueryForm;
import net.lab1024.sa.admin.module.business.gateway.domain.form.GatewayUpdateForm;
import net.lab1024.sa.admin.module.business.gateway.domain.vo.GatewayDetailVO;
import net.lab1024.sa.admin.module.business.gateway.domain.vo.GatewayVO;
import net.lab1024.sa.admin.module.business.gateway.manager.GatewayManager;
import net.lab1024.sa.admin.module.business.networkcomponent.service.NetworkComponentService;
import net.lab1024.sa.admin.module.business.protocol.service.ProtocolService;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 设备网关 Service
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class GatewayService {

    @Resource
    private GatewayDao gatewayDao;

    @Resource
    private GatewayManager gatewayManager;

    @Resource
    private NetworkComponentService networkComponentService;

    @Resource
    private ProtocolService protocolService;

    /**
     * 添加
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> add(GatewayAddForm addForm) {
        GatewayEntity entity = SmartBeanUtil.copy(addForm, GatewayEntity.class);
        gatewayManager.save(entity);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> update(GatewayUpdateForm updateForm) {
        GatewayEntity entity = gatewayManager.getById(updateForm.getId());
        if (entity == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        GatewayEntity updateEntity = SmartBeanUtil.copy(updateForm, GatewayEntity.class);
        gatewayManager.updateById(updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> delete(Long id) {
        GatewayEntity entity = gatewayManager.getById(id);
        if (entity == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        gatewayManager.removeById(id);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ResponseDTO.ok();
        }
        gatewayDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 启用/禁用
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateStatus(Long id, Integer status) {
        GatewayEntity entity = gatewayManager.getById(id);
        if (entity == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        entity.setStatus(status);
        gatewayManager.updateById(entity);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询
     */
    public ResponseDTO<PageResult<GatewayVO>> queryPage(GatewayQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<GatewayVO> list = gatewayDao.queryPage(page, queryForm);
        PageResult<GatewayVO> pageResult = SmartPageUtil.convert2PageResult(page, list);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询详情（含网络组件和协议完整信息）
     */
    public GatewayDetailVO getDetail(Long id) {
        GatewayEntity entity = gatewayManager.getById(id);
        if (entity == null) {
            return null;
        }
        GatewayDetailVO detail = SmartBeanUtil.copy(entity, GatewayDetailVO.class);
        if (entity.getComponentId() != null) {
            detail.setNetworkComponent(networkComponentService.getById(entity.getComponentId()));
        }
        if (entity.getProtocolId() != null) {
            detail.setProtocol(protocolService.getById(entity.getProtocolId()));
        }
        return detail;
    }

    public long countByComponentId(Long componentId) {
        return gatewayManager.lambdaQuery()
                .eq(GatewayEntity::getComponentId, componentId)
                .count();
    }
}
