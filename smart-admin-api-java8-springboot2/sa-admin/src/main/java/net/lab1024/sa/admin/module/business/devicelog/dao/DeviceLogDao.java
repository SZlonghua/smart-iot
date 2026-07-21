package net.lab1024.sa.admin.module.business.devicelog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.devicelog.domain.entity.DeviceLogEntity;
import net.lab1024.sa.admin.module.business.devicelog.domain.form.DeviceLogQueryForm;
import net.lab1024.sa.admin.module.business.devicelog.domain.vo.DeviceLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备日志 DAO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Mapper
public interface DeviceLogDao extends BaseMapper<DeviceLogEntity> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询条件
     * @return 分页结果
     */
    List<DeviceLogVO> queryPage(Page page, @Param("queryForm") DeviceLogQueryForm queryForm);
}
