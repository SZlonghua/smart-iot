package net.lab1024.sa.admin.module.business.productcategory.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.admin.module.business.productcategory.domain.form.ProductCategoryAddForm;
import net.lab1024.sa.admin.module.business.productcategory.domain.form.ProductCategoryQueryForm;
import net.lab1024.sa.admin.module.business.productcategory.domain.form.ProductCategoryUpdateForm;
import net.lab1024.sa.admin.module.business.productcategory.domain.vo.ProductCategoryVO;
import net.lab1024.sa.admin.module.business.productcategory.service.ProductCategoryService;
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

/**
 * 产品分类 Controller
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@RestController
@Tag(name = "产品分类管理")
public class ProductCategoryController {

    @Resource
    private ProductCategoryService productCategoryService;

    @Operation(summary = "分页查询 @author 廖涛")
    @PostMapping("/productCategory/queryPage")
    @SaCheckPermission("productCategory:query")
    public ResponseDTO<PageResult<ProductCategoryVO>> queryPage(@RequestBody @Valid ProductCategoryQueryForm queryForm) {
        return ResponseDTO.ok(productCategoryService.queryPage(queryForm));
    }

    @Operation(summary = "查询分类树 @author 廖涛")
    @GetMapping("/productCategory/queryTree")
    @SaCheckPermission("productCategory:query")
    public ResponseDTO<List<ProductCategoryVO>> queryTree() {
        return ResponseDTO.ok(productCategoryService.queryTree());
    }

    @Operation(summary = "添加 @author 廖涛")
    @PostMapping("/productCategory/add")
    @SaCheckPermission("productCategory:add")
    public ResponseDTO<String> add(@RequestBody @Valid ProductCategoryAddForm addForm) {
        return productCategoryService.add(addForm);
    }

    @Operation(summary = "更新 @author 廖涛")
    @PostMapping("/productCategory/update")
    @SaCheckPermission("productCategory:update")
    public ResponseDTO<String> update(@RequestBody @Valid ProductCategoryUpdateForm updateForm) {
        return productCategoryService.update(updateForm);
    }

    @Operation(summary = "批量删除 @author 廖涛")
    @PostMapping("/productCategory/batchDelete")
    @SaCheckPermission("productCategory:delete")
    public ResponseDTO<String> batchDelete(@RequestBody @Valid ValidateList<Long> idList) {
        return productCategoryService.batchDelete(idList);
    }

    @Operation(summary = "单个删除 @author 廖涛")
    @GetMapping("/productCategory/delete/{id}")
    @SaCheckPermission("productCategory:delete")
    public ResponseDTO<String> delete(@PathVariable Long id) {
        return productCategoryService.delete(id);
    }
}
