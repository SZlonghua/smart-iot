package net.lab1024.sa.admin.module.business.networkcomponent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.entity.NetworkComponentEntity;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.form.NetworkComponentQueryForm;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.vo.NetworkComponentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 网络组件 DAO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Mapper
public interface NetworkComponentDao extends BaseMapper<NetworkComponentEntity> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询条件
     * @return 分页结果
     */
    List<NetworkComponentVO> queryPage(Page page, @Param("queryForm") NetworkComponentQueryForm queryForm);
}
