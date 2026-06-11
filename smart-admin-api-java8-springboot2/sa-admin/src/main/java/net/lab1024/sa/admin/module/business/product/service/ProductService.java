package net.lab1024.sa.admin.module.business.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.product.dao.ProductDao;
import net.lab1024.sa.admin.module.business.product.domain.entity.ProductEntity;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductAddForm;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductQueryForm;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductUpdateForm;
import net.lab1024.sa.admin.module.business.product.domain.vo.ProductVO;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.metadata.ParseResult;
import net.lab1024.sa.base.module.support.thingsmodel.IotThingsMetadataCodec;
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
     * 查询详情
     */
    public ProductVO getById(Long id) {
        ProductEntity entity = productDao.selectById(id);
        return SmartBeanUtil.copy(entity, ProductVO.class);
    }

    /**
     * 分页查询
     */
    public PageResult<ProductVO> queryPage(ProductQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<ProductVO> list = productDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加产品：自动生成 productKey / productSecret，物模型默认空结构
     */
    public ResponseDTO<String> add(ProductAddForm addForm) {
        ProductEntity entity = SmartBeanUtil.copy(addForm, ProductEntity.class);
        entity.setProductKey(generateProductKey());                                     // PK- 前缀 + 16位大写 UUID
        entity.setProductSecret(generateProductSecret());                                // 32位 UUID
        entity.setModelJson("{\"properties\":[],\"functions\":[],\"events\":[]}");       // 默认空物模型
        entity.setStatus(0);                                                              // 默认禁用，需手动启用
        productDao.insert(entity);
        return ResponseDTO.ok();
    }

    /**
     * 更新产品：modelJson 为 null 时保留已有物模型（编辑产品信息不覆盖物模型）
     */
    public ResponseDTO<String> update(ProductUpdateForm updateForm) {
        ProductEntity entity = SmartBeanUtil.copy(updateForm, ProductEntity.class);
        if (updateForm.getModelJson() == null) {
            ProductEntity existing = productDao.selectById(updateForm.getId());
            if (existing != null) {
                entity.setModelJson(existing.getModelJson());   // 保留已有物模型
            }
        }
        productDao.updateById(entity);
        return ResponseDTO.ok();
    }

    /**
     * 保存物模型：解析 → 校验 → 归一化 → 入库
     */
    public ResponseDTO<String> saveModelJson(Long id, String modelJson) {
        ParseResult r = IotThingsMetadataCodec.getInstance().parseAndValidate(modelJson);
        if (!r.isValid()) {
            return ResponseDTO.error(UserErrorCode.PARAM_ERROR, String.join("; ", r.getErrors()));
        }
        String canonical = IotThingsMetadataCodec.getInstance().encode(r.getMetadata());
        ProductEntity entity = new ProductEntity();
        entity.setId(id);
        entity.setModelJson(canonical);
        productDao.updateById(entity);
        return ResponseDTO.ok();
    }

    /**
     * 切换启用/禁用状态
     */
    public ResponseDTO<String> toggleStatus(Long id, Integer status) {
        ProductEntity entity = new ProductEntity();
        entity.setId(id);
        entity.setStatus(status);
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
