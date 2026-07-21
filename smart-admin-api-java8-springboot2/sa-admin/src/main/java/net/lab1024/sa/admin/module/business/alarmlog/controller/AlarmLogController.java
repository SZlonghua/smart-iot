package net.lab1024.sa.admin.module.business.alarmlog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.alarmlog.domain.form.AlarmLogHandleForm;
import net.lab1024.sa.admin.module.business.alarmlog.domain.form.AlarmLogQueryForm;
import net.lab1024.sa.admin.module.business.alarmlog.domain.vo.AlarmLogVO;
import net.lab1024.sa.admin.module.business.alarmlog.service.AlarmLogService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 告警日志 Controller
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.MANAGER_ALARM_LOG)
public class AlarmLogController {

    @Resource
    private AlarmLogService alarmLogService;

    @Operation(summary = "分页查询告警日志")
    @PostMapping("/alarmLog/queryPage")
    @SaCheckPermission("alarmLog:query")
    public ResponseDTO<PageResult<AlarmLogVO>> queryPage(@RequestBody @Valid AlarmLogQueryForm queryForm) {
        return alarmLogService.queryPage(queryForm);
    }

    @Operation(summary = "处理告警")
    @PostMapping("/alarmLog/handle")
    @SaCheckPermission("alarmLog:handle")
    public ResponseDTO<String> handle(@RequestBody @Valid AlarmLogHandleForm handleForm) {
        return alarmLogService.handle(handleForm);
    }
}
