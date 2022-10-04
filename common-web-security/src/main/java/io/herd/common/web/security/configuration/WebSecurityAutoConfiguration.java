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
package io.herd.common.web.security.configuration;

import io.herd.common.web.security.jwt.JwtTokenExtractor;
import io.herd.common.web.security.jwt.authentication.DefaultJwtAuthenticationConverter;
import io.herd.common.web.security.jwt.authentication.DefaultJwtAuthenticationTokenConfigurer;
import io.herd.common.web.security.jwt.authentication.JwtAuthenticationTokenConfigurer;
import io.herd.common.web.security.jwt.authorization.DefaultJwtTokenExtractor;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

@Configuration
@ConditionalOnWebApplication
// Do not add the auto-configured classes, otherwise the auto-configuration will not work as expected.
@ComponentScan(basePackages = "io.herd.common.web.security",
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
)
@PropertySource("classpath:application-common-web-security.properties")
@Import(WebSecurityConfigurerAdapterAutoConfiguration.class) // To be used with @WebMvcTest
public class WebSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthenticationConverter.class)
    @ConditionalOnProperty(name = "spring.web.security.jwt.authentication.enabled", havingValue = "true")
    public AuthenticationConverter authenticationConverter() {
        return new DefaultJwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationTokenConfigurer.class)
    @ConditionalOnProperty(name = "spring.web.security.jwt.authentication.enabled", havingValue = "true")
    public JwtAuthenticationTokenConfigurer jwtAuthenticationTokenConfigurer() {
        return new DefaultJwtAuthenticationTokenConfigurer();
    }

    @Bean
    @ConditionalOnMissingBean(JwtTokenExtractor.class)
    @ConditionalOnProperty(name = "spring.web.security.jwt.authorization.enabled", havingValue = "true")
    public JwtTokenExtractor<Authentication> jwtTokenExtractor() {
        return new DefaultJwtTokenExtractor();
    }
}
