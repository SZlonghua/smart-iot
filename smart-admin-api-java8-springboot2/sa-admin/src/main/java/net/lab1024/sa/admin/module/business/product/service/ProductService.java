package net.lab1024.sa.admin.module.business.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.product.dao.ProductDao;
import net.lab1024.sa.admin.module.business.product.domain.entity.ProductEntity;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductAddForm;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductQueryForm;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductUpdateForm;
import net.lab1024.sa.admin.module.business.product.domain.vo.ProductVO;
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
 * 产品 Service
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class ProductService {

    @Resource
    private ProductDao productDao;

    /**
     * 分页查询
     */
    public PageResult<ProductVO> queryPage(ProductQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<ProductVO> list = productDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(ProductAddForm addForm) {
        ProductEntity entity = SmartBeanUtil.copy(addForm, ProductEntity.class);
        entity.setProductKey(generateProductKey());
        entity.setProductSecret(generateProductSecret());
        entity.setStatus(1);
        productDao.insert(entity);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     */
    public ResponseDTO<String> update(ProductUpdateForm updateForm) {
        ProductEntity entity = SmartBeanUtil.copy(updateForm, ProductEntity.class);
        productDao.updateById(entity);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ResponseDTO.ok();
        }
        productDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(Long id) {
        if (null == id) {
            return ResponseDTO.ok();
        }
        productDao.deleteById(id);
        return ResponseDTO.ok();
    }

    private String generateProductKey() {
        return "PK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String generateProductSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
