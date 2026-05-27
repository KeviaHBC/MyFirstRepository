package com.example.scaffold.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/scaffold", "root", "root")
                .globalConfig(builder -> builder
                        .author("scaffold")
                        .outputDir("src/main/java")
                        .commentDate("yyyy-MM-dd"))
                .packageConfig(builder -> builder
                        .parent("com.example.scaffold.module")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, "src/main/resources/mapper")))
                .strategyConfig(builder -> builder
                        .addTablePrefix("t_")
                        .entityBuilder()
                        .enableLombok()
                        .enableTableFieldAnnotation()
                        .controllerBuilder()
                        .enableRestStyle())
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
