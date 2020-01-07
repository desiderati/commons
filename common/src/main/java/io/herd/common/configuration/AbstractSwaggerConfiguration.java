/*
 * Copyright (c) 2020 - Felipe Desiderati
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.herd.common.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Utilizar esta configuração sempre que precisar expor os controladores
 * ({@link RestController}) da
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
            .paths(pathsToExpose()::test)
            .build();
    }

    protected Set<String> protocols() {
        Set<String> protocols = new HashSet<>();
        protocols.add("http");
        protocols.add("https");
        return protocols;
    }

    @SuppressWarnings("WeakerAccess") // Must be protected!
    protected Predicate<String> pathsToExpose() {
        if (StringUtils.isBlank(pathToExpose())) {
            return PathSelectors.any()::apply;
        }
        return PathSelectors.regex(pathToExpose())::apply;
    }

    @SuppressWarnings("WeakerAccess") // Must be protected!
    protected String pathToExpose() {
        return null;
    }

    protected abstract String packagesToScan();
}
