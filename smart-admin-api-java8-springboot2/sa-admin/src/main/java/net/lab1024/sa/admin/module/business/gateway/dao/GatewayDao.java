package net.lab1024.sa.admin.module.business.gateway.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.gateway.domain.entity.GatewayEntity;
import net.lab1024.sa.admin.module.business.gateway.domain.form.GatewayQueryForm;
import net.lab1024.sa.admin.module.business.gateway.domain.vo.GatewayVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备网关 DAO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Mapper
public interface GatewayDao extends BaseMapper<GatewayEntity> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询条件
     * @return 分页结果
     */
    List<GatewayVO> queryPage(Page page, @Param("queryForm") GatewayQueryForm queryForm);
}
