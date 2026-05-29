package net.lab1024.sa.admin.module.business.devicelog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.devicelog.dao.DeviceLogDao;
import net.lab1024.sa.admin.module.business.devicelog.domain.form.DeviceLogQueryForm;
import net.lab1024.sa.admin.module.business.devicelog.domain.vo.DeviceLogVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 设备日志 Service
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
@Slf4j
public class DeviceLogService {

    @Resource
    private DeviceLogDao deviceLogDao;

    /**
     * 分页查询设备日志
     */
    public ResponseDTO<PageResult<DeviceLogVO>> queryPage(DeviceLogQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<DeviceLogVO> list = deviceLogDao.queryPage(page, queryForm);
        PageResult<DeviceLogVO> pageResult = SmartPageUtil.convert2PageResult(page, list);
        return ResponseDTO.ok(pageResult);
    }
}
