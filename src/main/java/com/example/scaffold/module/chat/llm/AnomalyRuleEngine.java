package com.example.scaffold.module.chat.llm;

import com.example.scaffold.module.article.entity.Article;
import com.example.scaffold.module.article.service.ArticleService;
import com.example.scaffold.module.chat.dto.AnomalyInfo;
import com.example.scaffold.module.product.entity.Product;
import com.example.scaffold.module.product.service.ProductService;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 规则引擎：对每个模块的数据逐条应用检测规则，生成异常列表。
 */
@Component
@RequiredArgsConstructor
public class AnomalyRuleEngine {

    private final ProductService productService;
    private final UserService userService;
    private final ArticleService articleService;

    public List<AnomalyInfo> analyze(String context) {
        if (context == null) return List.of();

        return switch (context) {
            case "product" -> analyzeProducts();
            case "user" -> analyzeUsers();
            case "article" -> analyzeArticles();
            default -> List.of();
        };
    }

    private List<AnomalyInfo> analyzeProducts() {
        List<AnomalyInfo> results = new ArrayList<>();
        List<Product> products = productService.list();

        for (Product p : products) {
            if (p.getPrice() != null && p.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                results.add(AnomalyInfo.builder()
                        .type("价格异常").field("price")
                        .actualValue(p.getPrice().toString())
                        .expectedValue("> 0").severity("严重")
                        .recordId(p.getId()).recordName(p.getName())
                        .suggestion("价格不能为零或负数，请检查数据录入")
                        .build());
            }

            if (p.getStock() != null && p.getStock() < 0) {
                results.add(AnomalyInfo.builder()
                        .type("库存不足").field("stock")
                        .actualValue(String.valueOf(p.getStock()))
                        .expectedValue("≥ 0").severity("警告")
                        .recordId(p.getId()).recordName(p.getName())
                        .suggestion("库存数量不能为负数，请核实库存数据")
                        .build());
            }
        }

        return results;
    }

    private List<AnomalyInfo> analyzeUsers() {
        List<AnomalyInfo> results = new ArrayList<>();
        List<User> users = userService.list();

        for (User u : users) {
            if (u.getEmail() == null || u.getEmail().isBlank()) {
                results.add(AnomalyInfo.builder()
                        .type("缺少邮箱").field("email")
                        .actualValue("（空）").expectedValue("有效的邮箱地址")
                        .severity("警告").recordId(u.getId()).recordName(u.getUsername())
                        .suggestion("请为用户补充邮箱地址，以便接收通知")
                        .build());
            }

            if (u.getUsername() != null && u.getUsername().length() > 50) {
                results.add(AnomalyInfo.builder()
                        .type("用户名过长").field("username")
                        .actualValue(u.getUsername().length() + " 字符")
                        .expectedValue("≤ 50 字符").severity("提示")
                        .recordId(u.getId()).recordName(u.getUsername())
                        .suggestion("建议缩短用户名至50字符以内")
                        .build());
            }
        }

        return results;
    }

    private List<AnomalyInfo> analyzeArticles() {
        List<AnomalyInfo> results = new ArrayList<>();
        List<Article> articles = articleService.list();

        for (Article a : articles) {
            if (a.getTitle() == null || a.getTitle().isBlank()) {
                results.add(AnomalyInfo.builder()
                        .type("标题为空").field("title")
                        .actualValue("（空）").expectedValue("非空标题")
                        .severity("严重").recordId(a.getId())
                        .recordName("文章#" + a.getId())
                        .suggestion("文章标题不能为空，请补充标题")
                        .build());
            }
        }

        return results;
    }
}
