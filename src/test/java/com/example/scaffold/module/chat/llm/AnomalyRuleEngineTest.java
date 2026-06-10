package com.example.scaffold.module.chat.llm;

import com.example.scaffold.module.article.entity.Article;
import com.example.scaffold.module.article.service.ArticleService;
import com.example.scaffold.module.chat.dto.AnomalyInfo;
import com.example.scaffold.module.product.entity.Product;
import com.example.scaffold.module.product.service.ProductService;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnomalyRuleEngine 单元测试")
class AnomalyRuleEngineTest {

    @Mock private ProductService productService;
    @Mock private UserService userService;
    @Mock private ArticleService articleService;

    @InjectMocks
    private AnomalyRuleEngine engine;

    @Nested
    @DisplayName("商品异常检测")
    class ProductAnalysis {

        @Test
        @DisplayName("价格为负数时检测到价格异常")
        void shouldDetectNegativePrice() {
            Product p = new Product();
            p.setId(1L);
            p.setName("问题商品");
            p.setPrice(new BigDecimal("-99"));
            p.setStock(10);

            when(productService.list()).thenReturn(List.of(p));

            List<AnomalyInfo> results = engine.analyze("product");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("价格异常");
            assertThat(results.get(0).getSeverity()).isEqualTo("严重");
            assertThat(results.get(0).getField()).isEqualTo("price");
        }

        @Test
        @DisplayName("库存为负数时检测到库存不足")
        void shouldDetectNegativeStock() {
            Product p = new Product();
            p.setId(2L);
            p.setName("缺货商品");
            p.setPrice(new BigDecimal("100"));
            p.setStock(-5);

            when(productService.list()).thenReturn(List.of(p));

            List<AnomalyInfo> results = engine.analyze("product");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("库存不足");
            assertThat(results.get(0).getSeverity()).isEqualTo("警告");
        }

        @Test
        @DisplayName("正常商品不产生异常")
        void shouldReturnEmptyForValidProducts() {
            Product p = new Product();
            p.setId(3L);
            p.setName("正常商品");
            p.setPrice(new BigDecimal("99"));
            p.setStock(50);

            when(productService.list()).thenReturn(List.of(p));

            List<AnomalyInfo> results = engine.analyze("product");
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("空商品列表不产生异常")
        void shouldReturnEmptyForEmptyProductList() {
            when(productService.list()).thenReturn(List.of());
            assertThat(engine.analyze("product")).isEmpty();
        }
    }

    @Nested
    @DisplayName("用户异常检测")
    class UserAnalysis {

        @Test
        @DisplayName("邮箱为空时检测到缺少邮箱")
        void shouldDetectMissingEmail() {
            User u = new User();
            u.setId(1L);
            u.setUsername("testuser");
            u.setEmail(null);

            when(userService.list()).thenReturn(List.of(u));

            List<AnomalyInfo> results = engine.analyze("user");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("缺少邮箱");
            assertThat(results.get(0).getSeverity()).isEqualTo("警告");
        }

        @Test
        @DisplayName("用户名超过50字符时检测到异常")
        void shouldDetectLongUsername() {
            User u = new User();
            u.setId(2L);
            u.setUsername("a".repeat(51));
            u.setEmail("test@example.com");

            when(userService.list()).thenReturn(List.of(u));

            List<AnomalyInfo> results = engine.analyze("user");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("用户名过长");
            assertThat(results.get(0).getSeverity()).isEqualTo("提示");
        }
    }

    @Nested
    @DisplayName("文章异常检测")
    class ArticleAnalysis {

        @Test
        @DisplayName("标题为空时检测到异常")
        void shouldDetectEmptyTitle() {
            Article a = new Article();
            a.setId(1L);
            a.setTitle(null);

            when(articleService.list()).thenReturn(List.of(a));

            List<AnomalyInfo> results = engine.analyze("article");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("标题为空");
            assertThat(results.get(0).getSeverity()).isEqualTo("严重");
        }
    }

    @Test
    @DisplayName("未知 context 返回空列表")
    void shouldReturnEmptyForUnknownContext() {
        assertThat(engine.analyze("unknown")).isEmpty();
    }

    @Test
    @DisplayName("null context 返回空列表")
    void shouldReturnEmptyForNullContext() {
        assertThat(engine.analyze(null)).isEmpty();
    }
}
