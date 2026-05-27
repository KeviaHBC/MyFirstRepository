package com.example.scaffold.module;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    @GetMapping("/page/user")
    public String userList() {
        return "user/list";
    }

    @GetMapping("/page/article")
    public String articleList() {
        return "article/list";
    }

    @GetMapping("/page/product")
    public String productList() {
        return "product/list";
    }
}
