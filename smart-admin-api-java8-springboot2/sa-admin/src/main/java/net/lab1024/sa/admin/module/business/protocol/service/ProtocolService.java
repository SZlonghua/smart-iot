package net.lab1024.sa.admin.module.business.protocol.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.protocol.dao.ProtocolDao;
import net.lab1024.sa.admin.module.business.protocol.domain.entity.ProtocolEntity;
import net.lab1024.sa.admin.module.business.protocol.domain.form.ProtocolAddForm;
import net.lab1024.sa.admin.module.business.protocol.domain.form.ProtocolQueryForm;
import net.lab1024.sa.admin.module.business.protocol.domain.form.ProtocolUpdateForm;
import net.lab1024.sa.admin.module.business.protocol.domain.vo.ProtocolVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 协议 Service
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class ProtocolService {

    @Resource
    private ProtocolDao protocolDao;

    /**
     * 分页查询
     */
    public PageResult<ProtocolVO> queryPage(ProtocolQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<ProtocolVO> list = protocolDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(ProtocolAddForm addForm) {
        ProtocolEntity entity = SmartBeanUtil.copy(addForm, ProtocolEntity.class);
        protocolDao.insert(entity);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     */
    public ResponseDTO<String> update(ProtocolUpdateForm updateForm) {
        ProtocolEntity entity = SmartBeanUtil.copy(updateForm, ProtocolEntity.class);
        protocolDao.updateById(entity);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ResponseDTO.ok();
        }
        protocolDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(Long id) {
        if (null == id) {
            return ResponseDTO.ok();
        }
        protocolDao.deleteById(id);
        return ResponseDTO.ok();
    }
}
