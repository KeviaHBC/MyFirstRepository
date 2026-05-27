package com.example.scaffold.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.scaffold.common.annotation.Log;
import com.example.scaffold.common.result.R;
import com.example.scaffold.module.product.entity.Product;
import com.example.scaffold.module.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/page")
    public R<Page<Product>> page(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Product::getName, name);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return R.ok(productService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public R<Product> getById(@PathVariable Long id) {
        return R.ok(productService.getById(id));
    }

    @Log("新增商品")
    @PostMapping
    public R<Void> save(@Valid @RequestBody Product product) {
        productService.save(product);
        return R.ok();
    }

    @Log("更新商品")
    @PutMapping
    public R<Void> update(@Valid @RequestBody Product product) {
        productService.updateById(product);
        return R.ok();
    }

    @Log("删除商品")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return R.ok();
    }
}
