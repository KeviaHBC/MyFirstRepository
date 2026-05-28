package com.example.scaffold.module.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.scaffold.common.annotation.Log;
import com.example.scaffold.common.result.R;
import com.example.scaffold.module.article.entity.Article;
import com.example.scaffold.module.article.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文章管理")
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "分页查询文章")
    @GetMapping("/page")
    public R<Page<Article>> page(@Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
                                  @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
                                  @Parameter(description = "标题") @RequestParam(required = false) String title) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(Article::getTitle, title);
        }
        wrapper.orderByDesc(Article::getCreateTime);
        return R.ok(articleService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "根据ID查询文章")
    @GetMapping("/{id}")
    public R<Article> getById(@Parameter(description = "文章ID") @PathVariable Long id) {
        return R.ok(articleService.getById(id));
    }

    @Operation(summary = "新增文章")
    @Log("新增文章")
    @PostMapping
    public R<Void> save(@Valid @RequestBody Article article) {
        articleService.save(article);
        return R.ok();
    }

    @Operation(summary = "更新文章")
    @Log("更新文章")
    @PutMapping
    public R<Void> update(@Valid @RequestBody Article article) {
        articleService.updateById(article);
        return R.ok();
    }

    @Operation(summary = "删除文章")
    @Log("删除文章")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "文章ID") @PathVariable Long id) {
        articleService.removeById(id);
        return R.ok();
    }
}
