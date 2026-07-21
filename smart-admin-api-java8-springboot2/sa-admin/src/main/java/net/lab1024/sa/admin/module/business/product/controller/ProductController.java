package net.lab1024.sa.admin.module.business.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductAddForm;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductModelSaveForm;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductQueryForm;
import net.lab1024.sa.admin.module.business.product.domain.form.ProductUpdateForm;
import net.lab1024.sa.admin.module.business.product.domain.vo.ProductVO;
import net.lab1024.sa.admin.module.business.product.service.ProductService;
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
 * 产品 Controller
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@RestController
@Tag(name = "产品管理")
public class ProductController {

    @Resource
    private ProductService productService;

    @Operation(summary = "分页查询 @author 廖涛")
    @PostMapping("/product/queryPage")
    @SaCheckPermission("product:query")
    public ResponseDTO<PageResult<ProductVO>> queryPage(@RequestBody @Valid ProductQueryForm queryForm) {
        return ResponseDTO.ok(productService.queryPage(queryForm));
    }

    @Operation(summary = "查询详情 @author 廖涛")
    @GetMapping("/product/getById/{id}")
    @SaCheckPermission("product:query")
    public ResponseDTO<ProductVO> getById(@PathVariable Long id) {
        return ResponseDTO.ok(productService.getById(id));
    }

    @Operation(summary = "添加 @author 廖涛")
    @PostMapping("/product/add")
    @SaCheckPermission("product:add")
    public ResponseDTO<String> add(@RequestBody @Valid ProductAddForm addForm) {
        return productService.add(addForm);
    }

    /**
     * 保存物模型：独立接口，仅更新 id + modelJson，不校验 name/deviceType
     */
    @Operation(summary = "保存物模型 @author 廖涛")
    @PostMapping("/product/saveModel")
    @SaCheckPermission("product:update")
    public ResponseDTO<String> saveModel(@RequestBody @Valid ProductModelSaveForm form) {
        return productService.saveModelJson(form.getId(), form.getModelJson());
    }

    /**
     * 切换启用/禁用状态
     */
    @Operation(summary = "切换启用/禁用 @author 廖涛")
    @PostMapping("/product/toggleStatus")
    @SaCheckPermission("product:update")
    public ResponseDTO<String> toggleStatus(@RequestBody ProductUpdateForm form) {
        return productService.toggleStatus(form.getId(), form.getStatus());
    }

    @Operation(summary = "更新 @author 廖涛")
    @PostMapping("/product/update")
    @SaCheckPermission("product:update")
    public ResponseDTO<String> update(@RequestBody @Valid ProductUpdateForm updateForm) {
        return productService.update(updateForm);
    }

    @Operation(summary = "批量删除 @author 廖涛")
    @PostMapping("/product/batchDelete")
    @SaCheckPermission("product:delete")
    public ResponseDTO<String> batchDelete(@RequestBody @Valid ValidateList<Long> idList) {
        return productService.batchDelete(idList);
    }

    @Operation(summary = "单个删除 @author 廖涛")
    @GetMapping("/product/delete/{id}")
    @SaCheckPermission("product:delete")
    public ResponseDTO<String> delete(@PathVariable Long id) {
        return productService.delete(id);
    }
}
