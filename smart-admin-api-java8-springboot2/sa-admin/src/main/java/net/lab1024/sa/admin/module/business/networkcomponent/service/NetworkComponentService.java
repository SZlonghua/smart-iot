package net.lab1024.sa.admin.module.business.networkcomponent.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.networkcomponent.dao.NetworkComponentDao;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.entity.NetworkComponentEntity;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.form.NetworkComponentAddForm;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.form.NetworkComponentQueryForm;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.form.NetworkComponentUpdateForm;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.vo.NetworkComponentVO;
import net.lab1024.sa.admin.module.business.networkcomponent.manager.NetworkComponentManager;
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
 * 网络组件 Service
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class NetworkComponentService {

    @Resource
    private NetworkComponentDao networkComponentDao;

    @Resource
    private NetworkComponentManager networkComponentManager;

    /**
     * 添加网络组件
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> add(NetworkComponentAddForm addForm) {
        NetworkComponentEntity entity = SmartBeanUtil.copy(addForm, NetworkComponentEntity.class);
        networkComponentManager.save(entity);
        return ResponseDTO.ok();
    }

    /**
     * 更新网络组件
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> update(NetworkComponentUpdateForm updateForm) {
        NetworkComponentEntity entity = networkComponentManager.getById(updateForm.getId());
        if (entity == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        NetworkComponentEntity updateEntity = SmartBeanUtil.copy(updateForm, NetworkComponentEntity.class);
        networkComponentManager.updateById(updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 删除网络组件
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> delete(Long id) {
        NetworkComponentEntity entity = networkComponentManager.getById(id);
        if (entity == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        networkComponentManager.removeById(id);
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
        networkComponentDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询
     */
    public ResponseDTO<PageResult<NetworkComponentVO>> queryPage(NetworkComponentQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<NetworkComponentVO> list = networkComponentDao.queryPage(page, queryForm);
        PageResult<NetworkComponentVO> pageResult = SmartPageUtil.convert2PageResult(page, list);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 根据id查询
     */
    public NetworkComponentVO getById(Long id) {
        NetworkComponentEntity entity = networkComponentManager.getById(id);
        return entity == null ? null : SmartBeanUtil.copy(entity, NetworkComponentVO.class);
    }

}
