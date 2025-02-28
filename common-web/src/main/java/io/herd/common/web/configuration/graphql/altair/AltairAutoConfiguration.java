/*
 * Copyright (c) 2025 - Felipe Desiderati
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
package io.herd.common.web.configuration.graphql.altair;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.thymeleaf.TemplateEngine;

/**
 * Inspired by from <a href="https://github.com/graphql-java-kickstart/graphql-spring-boot">GraphQL Spring Boot Starters</a>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(value = "spring.graphql.altair.enabled", havingValue = "true")
@EnableConfigurationProperties({
    AltairConfigurationProperties.class,
    AltairConfigurationPropertiesOptions.class,
    AltairConfigurationPropertiesResources.class
})
@PropertySource("classpath:application-common-web-graphql-altair.properties")
public class AltairAutoConfiguration {

    @Bean
    public AltairController altairController(AltairIndexHtmlTemplate altairIndexHtmlTemplate) {
        return new AltairController(altairIndexHtmlTemplate);
    }

    @Bean
    public AltairIndexHtmlTemplate altairIndexHtmlTemplate(
        AltairConfigurationProperties altairConfigurationProperties,
        AltairConfigurationPropertiesOptions altairOptions,
        AltairConfigurationPropertiesResources altairResources,
        TemplateEngine templateEngine
    ) {
        return new AltairIndexHtmlTemplate(
            altairConfigurationProperties,
            altairOptions,
            altairResources,
            templateEngine
        );
    }
}
