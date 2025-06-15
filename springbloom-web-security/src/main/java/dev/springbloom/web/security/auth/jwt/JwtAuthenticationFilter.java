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
package dev.springbloom.web.security.auth.jwt;

import dev.springbloom.web.security.auth.jwt.JwtAuthenticationClaimsConfigurer;
import dev.springbloom.web.security.configuration.WebSecurityAutoConfiguration;
import dev.springbloom.web.security.auth.jwt.JwtService;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

/**
 * A self-contained authentication which will read the user/password from request,
 * authenticate the user against an {@link AuthenticationManager} and generate a JWT Token.
 * <p>
 * To use this {@link Filter}, it will be necessary to define your own {@link AuthenticationManager} implementation,
 * or configure the default one to use a customized {@link UserDetailsService}.
 * <p>
 * This approach ensures you don't need an OAuth2 Authorization Server to be configured.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.web.security.jwt.authentication.enabled", havingValue = "true")
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * Class used for retrieving the user/password from the request. This information will be
     * used by the {@link AuthenticationManager} to authenticate the user.
     */
    private AuthenticationConverter authenticationConverter;
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    private JwtAuthenticationClaimsConfigurer jwtAuthenticationClaimsConfigurer;
    private JwtAuthenticationHeaderConfigurer jwtAuthenticationHeaderConfigurer;

    @Autowired
    public JwtAuthenticationFilter(
        JwtService jwtService,
        @Value("${spring.web.security.jwt.authentication.base-path-login:/authenticate}") String loginUrl
    ) {
        // Indicates whether this filter should attempt to process a login request.
        super(new AntPathRequestMatcher(loginUrl, "POST"));
        setAuthenticationSuccessHandler((request, response, authentication) -> {
            String authorizationHeader = null;
            if (authentication instanceof JwtAuthenticationToken) {
                authorizationHeader = ((JwtAuthenticationToken) authentication).authorizationHeader;
            }

            if (authorizationHeader == null) {
                // It means that this system should generate the authorization header.
                String jwtToken =
                    jwtService.generateToken(
                        jwtAuthenticationClaimsConfigurer.retrieveJwtClaimsConfigurer(request, authentication)
                    );

                jwtAuthenticationHeaderConfigurer.configureAuthorizationHeader(
                    response,
                    jwtAuthenticationHeaderConfigurer.configureBearerToken(jwtToken)
                );
            } else {
                // It means the authorization header was generated by the delegated authentication provider.
                jwtAuthenticationHeaderConfigurer.configureAuthorizationHeader(response, authorizationHeader);
            }
        });
        setContinueChainBeforeSuccessfulAuthentication(false);
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
    public void setJwtAuthenticationClaimsConfigurer(
        JwtAuthenticationClaimsConfigurer jwtAuthenticationClaimsConfigurer
    ) {
        this.jwtAuthenticationClaimsConfigurer = jwtAuthenticationClaimsConfigurer;
    }

    @Autowired // Prevent circular dependency.
    public void setJwtAuthenticationHeaderConfigurer(
        JwtAuthenticationHeaderConfigurer jwtAuthenticationHeaderConfigurer
    ) {
        this.jwtAuthenticationHeaderConfigurer = jwtAuthenticationHeaderConfigurer;
    }

    /**
     * By default, Spring Boot creates all beans of type: {@link Filter}. We do not want this filter
     * to be registered automatically because it needs to be registered via Spring Security configuration.
     * So preventing it from being run twice!
     *
     * @see WebSecurityAutoConfiguration
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> authenticationClientFilterRegistration(
        @Qualifier("jwtAuthenticationFilter") JwtAuthenticationFilter filter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // We did not expose or obtain the bean AuthenticationManager directly because if we define it,
        // Spring Boot Security will not create a standard UserDetailsService.
        if (getAuthenticationManager() == null) {
            setAuthenticationManager(authenticationManagerBuilder.getObject());
        }
        return getAuthenticationManager().authenticate(authenticationConverter.convert(request));
    }
}
