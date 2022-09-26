/*
 * Copyright (c) 2022 - Felipe Desiderati
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
package io.herd.common.web.configuration;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * We need to define this {@link SpringBootServletInitializer} because the Spring Boot will only automatically
 * configure Spring's bootstrap context if we use the {@link org.springframework.boot.SpringApplication} class
 * to run the application.
 * <p>
 * Use this configuration whenever you are going to use an application server to distribute the application.
 * For example, WildFly.
 * <p>
 * From the documentation: <i>Note that a WebApplicationInitializer is only needed if you are building a war
 * file and deploying it. If you prefer to run an embedded container then you won't need this at all.</i>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public abstract class AbstractWebApplicationInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
        String externalPropertiesFile = getExternalPropertiesFile();
        if (externalPropertiesFile != null) {
            applicationBuilder.properties("spring.config.location=file:" + externalPropertiesFile);
        }
        return applicationBuilder.sources(getWebApplicationClass());
    }

    protected String getExternalPropertiesFile() {
        return null;
    }

    protected abstract Class<?> getWebApplicationClass();
}
