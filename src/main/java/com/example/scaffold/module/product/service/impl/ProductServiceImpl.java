package com.example.scaffold.module.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scaffold.module.product.entity.Product;
import com.example.scaffold.module.product.mapper.ProductMapper;
import com.example.scaffold.module.product.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
}
