package com.example.scaffold.module.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.scaffold.common.annotation.Log;
import com.example.scaffold.common.result.R;
import com.example.scaffold.module.article.entity.Article;
import com.example.scaffold.module.article.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/page")
    public R<Page<Article>> page(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String title) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(Article::getTitle, title);
        }
        wrapper.orderByDesc(Article::getCreateTime);
        return R.ok(articleService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public R<Article> getById(@PathVariable Long id) {
        return R.ok(articleService.getById(id));
    }

    @Log("新增文章")
    @PostMapping
    public R<Void> save(@Valid @RequestBody Article article) {
        articleService.save(article);
        return R.ok();
    }

    @Log("更新文章")
    @PutMapping
    public R<Void> update(@Valid @RequestBody Article article) {
        articleService.updateById(article);
        return R.ok();
    }

    @Log("删除文章")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        articleService.removeById(id);
        return R.ok();
    }
}
