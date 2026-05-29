package net.lab1024.sa.admin.module.business.devicelog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.devicelog.domain.form.DeviceLogQueryForm;
import net.lab1024.sa.admin.module.business.devicelog.domain.vo.DeviceLogVO;
import net.lab1024.sa.admin.module.business.devicelog.service.DeviceLogService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 设备日志 Controller
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.MANAGER_DEVICE_LOG)
public class DeviceLogController {

    @Resource
    private DeviceLogService deviceLogService;

    @Operation(summary = "分页查询设备日志")
    @PostMapping("/deviceLog/queryPage")
    @SaCheckPermission("deviceLog:query")
    public ResponseDTO<PageResult<DeviceLogVO>> queryPage(@RequestBody @Valid DeviceLogQueryForm queryForm) {
        return deviceLogService.queryPage(queryForm);
    }
}
