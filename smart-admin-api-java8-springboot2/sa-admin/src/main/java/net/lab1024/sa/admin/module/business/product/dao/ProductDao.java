package net.lab1024.sa.admin.module.business.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.product.domain.entity.ProductEntity;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductQueryForm;
import net.lab1024.sa.admin.module.business.product.domain.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品 DAO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Mapper
public interface ProductDao extends BaseMapper<ProductEntity> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询条件
     * @return 分页结果
     */
    List<ProductVO> queryPage(Page page, @Param("queryForm") ProductQueryForm queryForm);
}
