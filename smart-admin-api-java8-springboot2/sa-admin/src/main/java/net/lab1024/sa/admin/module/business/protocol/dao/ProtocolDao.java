package net.lab1024.sa.admin.module.business.protocol.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.protocol.domain.entity.ProtocolEntity;
import net.lab1024.sa.admin.module.business.protocol.domain.form.ProtocolQueryForm;
import net.lab1024.sa.admin.module.business.protocol.domain.vo.ProtocolVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 协议 DAO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Mapper
public interface ProtocolDao extends BaseMapper<ProtocolEntity> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询条件
     * @return 分页结果
     */
    List<ProtocolVO> queryPage(Page page, @Param("queryForm") ProtocolQueryForm queryForm);
}
