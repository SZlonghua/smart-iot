package net.lab1024.sa.admin.module.business.gateway.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.admin.module.business.gateway.domain.form.GatewayAddForm;
import net.lab1024.sa.admin.module.business.gateway.domain.form.GatewayQueryForm;
import net.lab1024.sa.admin.module.business.gateway.domain.form.GatewayUpdateForm;
import net.lab1024.sa.admin.module.business.gateway.domain.vo.GatewayDetailVO;
import net.lab1024.sa.admin.module.business.gateway.domain.vo.GatewayVO;
import net.lab1024.sa.admin.module.business.gateway.service.GatewayService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.ValidateList;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 设备网关 Controller
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@RestController
@Tag(name = "设备网关管理")
public class GatewayController {

    @Resource
    private GatewayService gatewayService;

    @Operation(summary = "分页查询设备网关 @author 1024")
    @PostMapping("/gateway/queryPage")
    @SaCheckPermission("gateway:query")
    public ResponseDTO<PageResult<GatewayVO>> queryPage(@RequestBody @Valid GatewayQueryForm queryForm) {
        return gatewayService.queryPage(queryForm);
    }

    @Operation(summary = "添加设备网关 @author 1024")
    @PostMapping("/gateway/add")
    @SaCheckPermission("gateway:add")
    public ResponseDTO<String> add(@RequestBody @Valid GatewayAddForm addForm) {
        return gatewayService.add(addForm);
    }

    @Operation(summary = "更新设备网关 @author 1024")
    @PostMapping("/gateway/update")
    @SaCheckPermission("gateway:update")
    public ResponseDTO<String> update(@RequestBody @Valid GatewayUpdateForm updateForm) {
        return gatewayService.update(updateForm);
    }

    @Operation(summary = "批量删除设备网关 @author 1024")
    @PostMapping("/gateway/batchDelete")
    @SaCheckPermission("gateway:delete")
    public ResponseDTO<String> batchDelete(@RequestBody @Valid ValidateList<Long> idList) {
        return gatewayService.batchDelete(idList);
    }

    @Operation(summary = "查询设备网关详情 @author 廖涛")
    @GetMapping("/gateway/getDetail/{id}")
    public ResponseDTO<GatewayDetailVO> getDetail(@PathVariable Long id) {
        return gatewayService.getDetail(id);
    }

    @Operation(summary = "启用/禁用设备网关 @author 廖涛")
    @GetMapping("/gateway/updateStatus/{id}/{status}")
    @SaCheckPermission("gateway:update")
    public ResponseDTO<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return gatewayService.updateStatus(id, status);
    }

    @Operation(summary = "删除设备网关 @author 1024")
    @GetMapping("/gateway/delete/{id}")
    @SaCheckPermission("gateway:delete")
    public ResponseDTO<String> delete(@PathVariable Long id) {
        return gatewayService.delete(id);
    }
}
