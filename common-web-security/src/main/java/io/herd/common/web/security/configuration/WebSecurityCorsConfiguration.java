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
package io.herd.common.web.security.configuration;

import io.herd.common.web.UrlUtils;
import io.herd.common.web.configuration.CorsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebSecurityCorsConfiguration implements WebMvcConfigurer {

    @Value("${spring.web.security.jwt.authentication.enabled:false}")
    private boolean jwtAuthenticationEnabled;

    @Value("${spring.web.security.jwt.authentication.login-url:/login}")
    private String jwtAuthenticationLoginUrl;

    private CorsProperties webSecurityCorsProperties;

    @Autowired(required = false)
    public void setWebSecurityCorsProperties(
        @Lazy @Qualifier("webSecurityCorsProperties") CorsProperties webSecurityCorsProperties
    ) {
        this.webSecurityCorsProperties = webSecurityCorsProperties;
    }

    @Validated
    @Bean("webSecurityCorsProperties")
    @ConfigurationProperties("spring.web.security.jwt.authentication.cors")
    public CorsProperties webSecurityCorsProperties() {
        return new CorsProperties();
    }

    /**
     * Set up Cross-Origin Resource Sharing (CORS).
     * <p>
     * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-cors-global">
     * Global CORS configuration
     * </a>
     */
    @Override
    public void addCorsMappings(final @NonNull CorsRegistry registry) {
        if (jwtAuthenticationEnabled) {
            webSecurityCorsProperties.addCorsMappings(
                registry,
                UrlUtils.appendDoubleAsterisk(jwtAuthenticationLoginUrl)
            );
        }
    }
}
