/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration;

import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashSet;
import java.util.Set;

/**
 * Utilizar esta configuração sempre que precisar expor os controladores
 * ({@link org.springframework.web.bind.annotation.RestController}) da
 * aplicação via Swagger.
 */
@EnableSwagger2
@SpringBootConfiguration
@PropertySource("classpath:swagger.properties")
@SuppressWarnings("unused")
public abstract class AbstractSwaggerConfiguration {

    @Bean
    public Docket api(@Value("${swagger.enabled}") boolean enabled) {
        return new Docket(DocumentationType.SWAGGER_2)
            .enable(enabled)
            .protocols(protocols())
            .select()
            .apis(RequestHandlerSelectors.basePackage(packagesToScan()))
            .paths(pathsToExpose())
            .build();
    }

    protected Set<String> protocols() {
        Set<String> protocols = new HashSet<>();
        protocols.add("http");
        protocols.add("https");
        return protocols;
    }

    protected Predicate<String> pathsToExpose() {
        if (StringUtils.isBlank(pathToExpose())) {
            return PathSelectors.any();
        }
        return PathSelectors.regex(pathToExpose());
    }

    protected String pathToExpose() {
        return null;
    }

    protected abstract String packagesToScan();
}
