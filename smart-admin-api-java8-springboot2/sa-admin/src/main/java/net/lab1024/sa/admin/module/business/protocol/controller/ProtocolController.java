package net.lab1024.sa.admin.module.business.protocol.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.admin.module.business.protocol.domain.form.ProtocolAddForm;
import net.lab1024.sa.admin.module.business.protocol.domain.form.ProtocolQueryForm;
import net.lab1024.sa.admin.module.business.protocol.domain.form.ProtocolUpdateForm;
import net.lab1024.sa.admin.module.business.protocol.domain.vo.ProtocolVO;
import net.lab1024.sa.admin.module.business.protocol.service.ProtocolService;
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

/**
 * 协议 Controller
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@RestController
@Tag(name = "协议管理")
public class ProtocolController {

    @Resource
    private ProtocolService protocolService;

    @Operation(summary = "分页查询 @author 廖涛")
    @PostMapping("/protocol/queryPage")
    @SaCheckPermission("protocol:query")
    public ResponseDTO<PageResult<ProtocolVO>> queryPage(@RequestBody @Valid ProtocolQueryForm queryForm) {
        return ResponseDTO.ok(protocolService.queryPage(queryForm));
    }

    @Operation(summary = "添加 @author 廖涛")
    @PostMapping("/protocol/add")
    @SaCheckPermission("protocol:add")
    public ResponseDTO<String> add(@RequestBody @Valid ProtocolAddForm addForm) {
        return protocolService.add(addForm);
    }

    @Operation(summary = "更新 @author 廖涛")
    @PostMapping("/protocol/update")
    @SaCheckPermission("protocol:update")
    public ResponseDTO<String> update(@RequestBody @Valid ProtocolUpdateForm updateForm) {
        return protocolService.update(updateForm);
    }

    @Operation(summary = "批量删除 @author 廖涛")
    @PostMapping("/protocol/batchDelete")
    @SaCheckPermission("protocol:delete")
    public ResponseDTO<String> batchDelete(@RequestBody @Valid ValidateList<Long> idList) {
        return protocolService.batchDelete(idList);
    }

    @Operation(summary = "单个删除 @author 廖涛")
    @GetMapping("/protocol/delete/{id}")
    @SaCheckPermission("protocol:delete")
    public ResponseDTO<String> delete(@PathVariable Long id) {
        return protocolService.delete(id);
    }
}
