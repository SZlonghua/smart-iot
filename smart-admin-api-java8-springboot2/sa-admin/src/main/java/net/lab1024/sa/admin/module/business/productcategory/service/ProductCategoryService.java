package net.lab1024.sa.admin.module.business.productcategory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.productcategory.dao.ProductCategoryDao;
import net.lab1024.sa.admin.module.business.productcategory.domain.entity.ProductCategoryEntity;
import net.lab1024.sa.admin.module.business.productcategory.domain.form.ProductCategoryAddForm;
import net.lab1024.sa.admin.module.business.productcategory.domain.form.ProductCategoryQueryForm;
import net.lab1024.sa.admin.module.business.productcategory.domain.form.ProductCategoryUpdateForm;
import net.lab1024.sa.admin.module.business.productcategory.domain.vo.ProductCategoryVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品分类 Service
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class ProductCategoryService {

    @Resource
    private ProductCategoryDao productCategoryDao;

    /**
     * 分页查询
     */
    public PageResult<ProductCategoryVO> queryPage(ProductCategoryQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<ProductCategoryVO> list = productCategoryDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询分类树
     */
    public List<ProductCategoryVO> queryTree() {
        List<ProductCategoryEntity> allList = productCategoryDao.selectList(null);
        List<ProductCategoryVO> voList = SmartBeanUtil.copyList(allList, ProductCategoryVO.class);
        return buildTree(voList, 0L);
    }

    private List<ProductCategoryVO> buildTree(List<ProductCategoryVO> allList, Long parentId) {
        Map<Long, List<ProductCategoryVO>> parentMap = allList.stream()
                .filter(e -> e.getParentId() != null)
                .collect(Collectors.groupingBy(ProductCategoryVO::getParentId));
        List<ProductCategoryVO> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        children.forEach(e -> e.setChildren(buildChildren(allList, parentMap, e.getId())));
        return children;
    }

    private List<ProductCategoryVO> buildChildren(List<ProductCategoryVO> allList, Map<Long, List<ProductCategoryVO>> parentMap, Long parentId) {
        List<ProductCategoryVO> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        if (CollectionUtils.isEmpty(children)) {
            return new ArrayList<>();
        }
        children.forEach(e -> e.setChildren(buildChildren(allList, parentMap, e.getId())));
        return children;
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(ProductCategoryAddForm addForm) {
        ProductCategoryEntity entity = SmartBeanUtil.copy(addForm, ProductCategoryEntity.class);
        if (entity.getParentId() == null) {
            entity.setParentId(0L);
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        productCategoryDao.insert(entity);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     */
    public ResponseDTO<String> update(ProductCategoryUpdateForm updateForm) {
        ProductCategoryEntity entity = SmartBeanUtil.copy(updateForm, ProductCategoryEntity.class);
        productCategoryDao.updateById(entity);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ResponseDTO.ok();
        }
        productCategoryDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(Long id) {
        if (null == id) {
            return ResponseDTO.ok();
        }
        productCategoryDao.deleteById(id);
        return ResponseDTO.ok();
    }
}
