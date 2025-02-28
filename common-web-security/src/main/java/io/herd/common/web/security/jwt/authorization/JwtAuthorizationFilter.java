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
package io.herd.common.web.security.jwt.authorization;

import io.herd.common.data.multitenant.MultiTenantContext;
import io.herd.common.web.security.MultiTenantSupport;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authorization via JWT Token.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.web.security.jwt.authorization.enabled", havingValue = "true")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private JwtAuthorizationService jwtAuthorizationService;

    @Autowired
    public void setJwtAuthorizationService(JwtAuthorizationService jwtAuthorizationService) {
        this.jwtAuthorizationService = jwtAuthorizationService;
    }

    /**
     * By default, Spring Boot creates all beans of type: {@link Filter}. We do not want this filter
     * to be registered automatically because it needs to be registered via Spring Security configuration.
     * So preventing it from being run twice!
     */
    @Bean
    public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
        @Qualifier("jwtAuthorizationFilter") JwtAuthorizationFilter filter
    ) {
        FilterRegistrationBean<JwtAuthorizationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest servletRequest,
        @NotNull HttpServletResponse servletResponse,
        @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Authentication authentication = null;
            try {
                authentication = jwtAuthorizationService.verifyAuthentication(servletRequest);
                if (authentication != null
                    && authentication.getPrincipal() instanceof MultiTenantSupport multiTenantSupport
                ) {
                    if (StringUtils.isNotBlank(multiTenantSupport.getTenant())) {
                        MultiTenantContext.set(multiTenantSupport.getTenant());
                    }
                }
            } catch (AuthenticationServiceException failed) {
                log.error("Authorization Failed: {}", failed.getMessage(), failed);
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.debug("SecurityContextHolder already contains: '{}'",
                SecurityContextHolder.getContext().getAuthentication()
            );
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MultiTenantContext.clear();
        }
    }
}
