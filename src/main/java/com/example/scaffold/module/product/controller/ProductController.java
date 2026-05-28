package com.example.scaffold.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.scaffold.common.annotation.Log;
import com.example.scaffold.common.result.R;
import com.example.scaffold.module.product.entity.Product;
import com.example.scaffold.module.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品管理")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页查询商品")
    @GetMapping("/page")
    public R<Page<Product>> page(@Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
                                  @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
                                  @Parameter(description = "商品名称") @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Product::getName, name);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return R.ok(productService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "根据ID查询商品")
    @GetMapping("/{id}")
    public R<Product> getById(@Parameter(description = "商品ID") @PathVariable Long id) {
        return R.ok(productService.getById(id));
    }

    @Operation(summary = "新增商品")
    @Log("新增商品")
    @PostMapping
    public R<Void> save(@Valid @RequestBody Product product) {
        productService.save(product);
        return R.ok();
    }

    @Operation(summary = "更新商品")
    @Log("更新商品")
    @PutMapping
    public R<Void> update(@Valid @RequestBody Product product) {
        productService.updateById(product);
        return R.ok();
    }

    @Operation(summary = "删除商品")
    @Log("删除商品")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "商品ID") @PathVariable Long id) {
        productService.removeById(id);
        return R.ok();
    }
}
