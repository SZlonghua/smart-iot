package net.lab1024.sa.admin.module.business.device.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.admin.module.business.device.domain.form.DeviceAddForm;
import net.lab1024.sa.admin.module.business.device.domain.form.DeviceQueryForm;
import net.lab1024.sa.admin.module.business.device.domain.form.DeviceUpdateForm;
import net.lab1024.sa.admin.module.business.device.domain.vo.DeviceDetailVO;
import net.lab1024.sa.admin.module.business.device.domain.vo.DevicePropertyVO;
import net.lab1024.sa.admin.module.business.device.domain.vo.DeviceVO;
import net.lab1024.sa.admin.module.business.device.service.DeviceService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.ValidateList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 设备 Controller
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@RestController
@Tag(name = "设备管理")
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    // ==================== CRUD ====================

    @Operation(summary = "分页查询 @author 廖涛")
    @PostMapping("/device/queryPage")
    @SaCheckPermission("device:query")
    public ResponseDTO<PageResult<DeviceVO>> queryPage(@RequestBody @Valid DeviceQueryForm queryForm) {
        return ResponseDTO.ok(deviceService.queryPage(queryForm));
    }

    @Operation(summary = "添加 @author 廖涛")
    @PostMapping("/device/add")
    @SaCheckPermission("device:add")
    public ResponseDTO<String> add(@RequestBody @Valid DeviceAddForm addForm) {
        return deviceService.add(addForm);
    }

    @Operation(summary = "更新 @author 廖涛")
    @PostMapping("/device/update")
    @SaCheckPermission("device:update")
    public ResponseDTO<String> update(@RequestBody @Valid DeviceUpdateForm updateForm) {
        return deviceService.update(updateForm);
    }

    @Operation(summary = "批量删除 @author 廖涛")
    @PostMapping("/device/batchDelete")
    @SaCheckPermission("device:delete")
    public ResponseDTO<String> batchDelete(@RequestBody @Valid ValidateList<Long> idList) {
        return deviceService.batchDelete(idList);
    }

    @Operation(summary = "单个删除 @author 廖涛")
    @GetMapping("/device/delete/{id}")
    @SaCheckPermission("device:delete")
    public ResponseDTO<String> delete(@PathVariable Long id) {
        return deviceService.delete(id);
    }

    @Operation(summary = "设备详情 @author 廖涛")
    @GetMapping("/device/detail/{id}")
    public ResponseDTO<DeviceDetailVO> detail(@PathVariable Long id) {
        return ResponseDTO.ok(deviceService.getDetail(id));
    }

    // ==================== 属性操作 ====================

    @Operation(summary = "读取设备属性（下发命令到设备） @author 廖涛")
    @PostMapping("/device/{deviceId}/properties/read")
    @SaCheckPermission("device:query")
    public ResponseDTO<Map<String, Object>> readProperties(@PathVariable String deviceId,
                                                           @RequestBody List<String> properties) {
        return ResponseDTO.ok(deviceService.readProperties(deviceId, properties));
    }

    @Operation(summary = "查询设备属性（查数据库） @author 廖涛")
    @PostMapping("/device/{deviceId}/properties")
    @SaCheckPermission("device:query")
    public ResponseDTO<List<DevicePropertyVO>> getProperties(@PathVariable Long deviceId,
                                                              @RequestBody List<String> properties) {
        return ResponseDTO.ok(deviceService.getProperties(deviceId, properties));
    }

    @Operation(summary = "设置设备属性（下发命令到设备） @author 廖涛")
    @PostMapping("/device/setting/{deviceId}/property")
    @SaCheckPermission("device:update")
    public ResponseDTO<Map<String, Object>> writeProperties(@PathVariable String deviceId,
                                                             @RequestBody Map<String, Object> properties) {
        return ResponseDTO.ok(deviceService.writeProperties(deviceId, properties));
    }

    // ==================== 功能调用 ====================

    @Operation(summary = "调用设备功能（下发命令到设备） @author 廖涛")
    @PostMapping("/device/invoked/{deviceId}/function/{functionId}")
    @SaCheckPermission("device:update")
    public ResponseDTO<Object> invokeFunction(@PathVariable String deviceId,
                                               @PathVariable String functionId,
                                               @RequestBody Map<String, Object> properties) {
        return ResponseDTO.ok(deviceService.invokeFunction(deviceId, functionId, properties));
    }
}
