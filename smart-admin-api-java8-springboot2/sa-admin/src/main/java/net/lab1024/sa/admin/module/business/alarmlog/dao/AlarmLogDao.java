package net.lab1024.sa.admin.module.business.alarmlog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.alarmlog.domain.entity.AlarmLogEntity;
import net.lab1024.sa.admin.module.business.alarmlog.domain.form.AlarmLogQueryForm;
import net.lab1024.sa.admin.module.business.alarmlog.domain.vo.AlarmLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 告警日志 DAO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Mapper
public interface AlarmLogDao extends BaseMapper<AlarmLogEntity> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询条件
     * @return 分页结果
     */
    List<AlarmLogVO> queryPage(Page page, @Param("queryForm") AlarmLogQueryForm queryForm);
}
