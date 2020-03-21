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
package io.herd.common.security.jwt.authentication;

import io.herd.common.security.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Authentication via JWT Token. To use this {@link Filter}, it will be necessary to define
 * your own {@link AuthenticationManager} implementation. Or configure the default one to use
 * a customized {@link UserDetailsService}.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "security.jwt.authentication.enabled", havingValue = "true")
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_BEARER = "Bearer ";

    /**
     * Class used for retrieving the user/password from the request. This information will be
     * used by the {@link AuthenticationManager} to authenticate the user.
     */
    private AuthenticationConverter authenticationConverter;
    private JwtAuthenticationTokenConfigurer jwtAuthenticationTokenConfigurer;
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService,
                                   @Value("${security.jwt.authentication.login-url:/api/v1/login}") String loginUrl) {

        // Indicates whether this filter should attempt to process a login request.
        super(new AntPathRequestMatcher(loginUrl, "POST"));
        setAuthenticationSuccessHandler((request, response, authentication) -> {
            String token =
                jwtService.generateToken(
                    jwtAuthenticationTokenConfigurer.retrieveJwtTokenConfigurer(request, authentication));
            response.addHeader(HEADER_AUTHORIZATION, TOKEN_BEARER + token);
        });
    }

    @Override
    public void afterPropertiesSet() {
        // Disabling default behavior because the authentication manager will be configured
        // after the method afterPropertiesSet().
    }

    @Autowired // Prevent circular dependency.
    public void setAuthenticationManager(AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @Autowired // Prevent circular dependency.
    public void setAuthenticationConverter(AuthenticationConverter authenticationConverter) {
        this.authenticationConverter = authenticationConverter;
    }

    @Autowired // Prevent circular dependency.
    public void setJwtAuthenticationTokenConfigurer(JwtAuthenticationTokenConfigurer jwtAuthenticationTokenConfigurer) {
        this.jwtAuthenticationTokenConfigurer = jwtAuthenticationTokenConfigurer;
    }

    /**
     * By default, Spring Boot creates all beans of type: {@link Filter}. We do not want this filter
     * to be registered automatically because it needs to be registered via Spring Security configuration.
     * So preventing it from being run twice!
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> authenticationClientFilterRegistration(
            @Qualifier("jwtAuthenticationFilter") JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // We did not expose or obtain the bean AuthenticationManager directly, because if we define it,
        // Spring Boot Security will not create a standard UserDetailsService.
        if (getAuthenticationManager() == null) {
            setAuthenticationManager(authenticationManagerBuilder.getObject());
        }
        return getAuthenticationManager().authenticate(authenticationConverter.convert(request));
    }
}
