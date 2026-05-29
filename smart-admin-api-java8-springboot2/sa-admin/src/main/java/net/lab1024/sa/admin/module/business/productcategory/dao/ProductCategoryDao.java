package net.lab1024.sa.admin.module.business.productcategory.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.productcategory.domain.entity.ProductCategoryEntity;
import net.lab1024.sa.admin.module.business.productcategory.domain.form.ProductCategoryQueryForm;
import net.lab1024.sa.admin.module.business.productcategory.domain.vo.ProductCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品分类 DAO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Mapper
public interface ProductCategoryDao extends BaseMapper<ProductCategoryEntity> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询条件
     * @return 分页结果
     */
    List<ProductCategoryVO> queryPage(Page page, @Param("queryForm") ProductCategoryQueryForm queryForm);
}
