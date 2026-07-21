package net.lab1024.sa.admin.module.business.networkcomponent.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.form.NetworkComponentAddForm;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.form.NetworkComponentQueryForm;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.form.NetworkComponentUpdateForm;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.vo.NetworkComponentVO;
import net.lab1024.sa.admin.module.business.networkcomponent.service.NetworkComponentService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.ValidateList;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 网络组件 Controller
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@RestController
@Tag(name = "网络组件管理")
public class NetworkComponentController {

    @Resource
    private NetworkComponentService networkComponentService;

    @Operation(summary = "分页查询网络组件 @author 1024")
    @PostMapping("/networkComponent/queryPage")
    @SaCheckPermission("networkComponent:query")
    public ResponseDTO<PageResult<NetworkComponentVO>> queryPage(@RequestBody @Valid NetworkComponentQueryForm queryForm) {
        return networkComponentService.queryPage(queryForm);
    }

    @Operation(summary = "添加网络组件 @author 1024")
    @PostMapping("/networkComponent/add")
    @SaCheckPermission("networkComponent:add")
    public ResponseDTO<String> add(@RequestBody @Valid NetworkComponentAddForm addForm) {
        return networkComponentService.add(addForm);
    }

    @Operation(summary = "更新网络组件 @author 1024")
    @PostMapping("/networkComponent/update")
    @SaCheckPermission("networkComponent:update")
    public ResponseDTO<String> update(@RequestBody @Valid NetworkComponentUpdateForm updateForm) {
        return networkComponentService.update(updateForm);
    }

    @Operation(summary = "批量删除网络组件 @author 1024")
    @PostMapping("/networkComponent/batchDelete")
    @SaCheckPermission("networkComponent:delete")
    public ResponseDTO<String> batchDelete(@RequestBody @Valid ValidateList<Long> idList) {
        return networkComponentService.batchDelete(idList);
    }

    @Operation(summary = "删除网络组件 @author 1024")
    @GetMapping("/networkComponent/delete/{id}")
    @SaCheckPermission("networkComponent:delete")
    public ResponseDTO<String> delete(@PathVariable Long id) {
        return networkComponentService.delete(id);
    }
}
