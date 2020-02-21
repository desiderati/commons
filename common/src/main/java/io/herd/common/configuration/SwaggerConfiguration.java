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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RestController;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Use this configuration whenever you need to expose application controllers ({@link RestController}) via Swagger.
 */
@EnableSwagger2WebMvc
@SpringBootConfiguration
@PropertySource("classpath:swagger.properties")
@Import({SpringDataRestConfiguration.class, BeanValidatorPluginsConfiguration.class})
public class SwaggerConfiguration {

    // I dont't know why but the default value is not being recognized.
    @Value("${springfox.swagger.enabled:true}")
    private boolean enabled;

    @Value("${springfox.swagger.package-to-scan:}")
    private String packagesToScan;

    @Value("${springfox.swagger.paths-to-expose:/api/v1/.*}")
    private String pathsToExpose;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .enable(enabled)
            .protocols(protocols())
            .select()
            .apis(RequestHandlerSelectors.basePackage(packagesToScan))
            .paths(pathsToExpose(pathsToExpose))
            .build();
    }

    protected Set<String> protocols() {
        Set<String> protocols = new HashSet<>();
        protocols.add("http");
        protocols.add("https");
        return protocols;
    }

    protected Predicate<String> pathsToExpose(String pathsToExpose) {
        if (StringUtils.isBlank(pathsToExpose)) {
            return PathSelectors.any();
        }
        String[] pathsToExposeArr = pathsToExpose.split(",");
        Predicate<String> pathSelector = null;
        for (String pathToExpose : pathsToExposeArr) {
            if (pathSelector == null) {
                pathSelector = PathSelectors.regex(pathToExpose);
            } else {
                pathSelector = pathSelector.or(PathSelectors.regex(pathToExpose));
            }
        }
        return pathSelector;
    }
}
