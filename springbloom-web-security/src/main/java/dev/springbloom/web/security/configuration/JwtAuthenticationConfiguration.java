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
package dev.springbloom.web.security.configuration;

import dev.springbloom.data.multitenant.MultiTenantSupport;
import dev.springbloom.web.UrlUtils;
import dev.springbloom.web.configuration.CorsProperties;
import dev.springbloom.web.security.jwt.JwtEncryptionMethod;
import dev.springbloom.web.security.jwt.JwtKeys;
import dev.springbloom.web.security.jwt.JwtService;
import dev.springbloom.web.security.jwt.authentication.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.stream.Collectors;

/**
 * Configuration class for self-contained JWT authentication.
 * <p>
 * In this setup, the application is fully responsible for validating user credentials
 * and issuing JWT tokens as part of the authentication response—without relying on external identity providers.
 * <p>
 * This configuration enables JWT-based authentication and provides various customization options
 * for token generation, validation, and security context management.
 */
@Configuration
@ConditionalOnClass(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter.class)
@ConditionalOnProperty(name = "spring.web.security.jwt.authentication.enabled", havingValue = "true")
public class JwtAuthenticationConfiguration implements WebMvcConfigurer {

    /**
     * Configuration class for HTTP clients security.
     * <p>
     * This class decorates HTTP clients (RestTemplate and RestClient) with authentication headers.
     * It automatically adds JWT authentication headers to outgoing requests when the current
     * security context contains an authenticated user with a JWT token.
     * <p>
     * This configuration is conditionally enabled when JWT authentication is enabled, and
     * HTTP clients decoration is enabled in the application properties.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(value = {
        "spring.web.http.clients.enabled",
        "spring.web.http.clients.decorate-with-auth-header",
    }, havingValue = "true")
    public static class HttpClientsSecurityConfiguration {

        /**
         * This constructor initializes the configuration and decorates the default HTTP clients
         * with authentication headers.
         * <p>
         * It adds an interceptor to the HTTP clients that automatically adds JWT authentication
         * headers to outgoing requests.
         *
         * @param defaultRestTemplate               The default RestTemplate bean
         * @param defaultRestClient                 The default RestClient bean
         * @param beanFactory                       The bean factory for managing beans
         * @param jwtAuthenticationHeaderConfigurer The configurer for JWT authentication headers
         */
        @Autowired
        public HttpClientsSecurityConfiguration(
            @Qualifier("defaultRestTemplate") RestTemplate defaultRestTemplate,
            @Qualifier("defaultRestClient") RestClient defaultRestClient,
            ConfigurableListableBeanFactory beanFactory,
            JwtAuthenticationHeaderConfigurer jwtAuthenticationHeaderConfigurer
        ) {
            ClientHttpRequestInterceptor authHeaderClientHttpRequestInterceptor =
                (request, body, execution) -> {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication == null) {
                        return execution.execute(request, body);
                    }

                    if (!(authentication.getCredentials() instanceof AbstractOAuth2Token token)) {
                        return execution.execute(request, body);
                    }

                    jwtAuthenticationHeaderConfigurer.configureAuthorizationHeader(
                        request,
                        jwtAuthenticationHeaderConfigurer.configureBearerToken(token.getTokenValue())
                    );
                    return execution.execute(request, body);
                };

            defaultRestTemplate.getInterceptors().add(authHeaderClientHttpRequestInterceptor);
            if (defaultRestClient != null) {
                var decoratedDefaultRestClient = defaultRestClient.mutate().requestInterceptor(
                    authHeaderClientHttpRequestInterceptor
                ).build();

                ((DefaultListableBeanFactory) beanFactory).destroySingleton("defaultRestClient");
                beanFactory.autowireBean(decoratedDefaultRestClient);
            }
        }
    }

    @Value("${spring.web.security.jwt.authentication.base-path-login:/authenticate}")
    private String jwtAuthenticationBasePathLogin;

    @Value("${spring.web.security.jwt.authentication.authorities.parameter:authorities}")
    private String jwtAuthenticationAuthoritiesParameter;

    @Value("${spring.web.security.jwt.authentication.delegation.base-path-url:}")
    private String jwtDelegateAuthenticationBasePathUrl;

    @Value("${spring.web.security.jwt.authentication.delegation.base-path-login:/authenticate}")
    private String jwtDelegateAuthenticationBasePathLogin;

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

    @Bean
    @ConfigurationProperties("spring.web.security.jwt.authentication.keys")
    public JwtKeys jwtKeys() {
        return new JwtKeys();
    }

    /**
     * Creates and configures the JWT service for handling token operations.
     * This service is responsible for generating, validating, and processing JWT tokens.
     * It supports different encryption methods and configurable expiration periods.
     *
     * @param jwtKeys                            The JWT keys for signing and verification
     * @param jwtConverter                       The converter for JWT authentication
     * @param jwtEncryptionMethod                The encryption method to use (default: asymmetric)
     * @param jwtExpirationPeriod                The token expiration period in days (default: 1)
     * @param jwtAuthenticationDelegationEnabled Whether JWT authentication delegation is enabled
     */
    @Bean
    @ConditionalOnMissingBean(JwtService.class)
    public JwtService jwtService(
        @Value("${spring.web.security.jwt.authentication.issuer:https://springbloom.dev/issuer}")
        String jwtIssuer,

        @Value("${spring.web.security.jwt.authentication.audience}")
        String jwtAudience,

        JwtKeys jwtKeys,
        org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtConverter,

        @Value("${spring.web.security.jwt.authentication.encryption-method:asymmetric}")
        JwtEncryptionMethod jwtEncryptionMethod,

        @Value("${spring.web.security.jwt.authentication.expiration-period:1}")
        int jwtExpirationPeriod,

        @Value("${spring.web.security.jwt.authentication.delegation.enabled:false}")
        boolean jwtAuthenticationDelegationEnabled
    ) {
        return new JwtService(
            jwtIssuer,
            jwtAudience,
            jwtKeys,
            jwtConverter,
            jwtEncryptionMethod,
            jwtExpirationPeriod,
            jwtAuthenticationDelegationEnabled
        );
    }

    /**
     * Creates a configurer for JWT authentication claims.
     * <p>
     * This configurer is responsible for setting up the claims in the JWT token,
     * including the subject (username), authorities, and other information if available.
     */
    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationClaimsConfigurer.class)
    public JwtAuthenticationClaimsConfigurer jwtAuthenticationClaimsConfigurer() {
        return (request, authentication) -> jwtClaimsSetBuilder -> {
            jwtClaimsSetBuilder.subject(((UserDetails) authentication.getPrincipal()).getUsername())
                .claim(jwtAuthenticationAuthoritiesParameter,
                    authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList())
                );

            // TODO Felipe Desiderati: Atualmente com a implementação que existe, esta parte será sempre falso!
            if (authentication instanceof MultiTenantSupport) {
                jwtClaimsSetBuilder.claim(
                    MultiTenantSupport.TENANT,
                    ((MultiTenantSupport) authentication).getTenant()
                );
            }

            return jwtClaimsSetBuilder.build();
        };
    }

    /**
     * This converter is responsible for converting the JWT token into an Authentication object.
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationConverter.class)
    public AuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }

    /**
     * This configurer is responsible for setting up the headers in the JWT token.
     */
    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationHeaderConfigurer.class)
    public JwtAuthenticationHeaderConfigurer jwtAuthenticationHeaderConfigurer() {
        return new JwtAuthenticationHeaderBearerTokenConfigurer();
    }

    /**
     * This provider is responsible for delegating authentication to another service.
     */
    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationDelegateProvider.class)
    @ConditionalOnProperty(name = "spring.web.security.jwt.authentication.delegation.enabled", havingValue = "true")
    public JwtAuthenticationDelegateProvider jwtDelegateAuthenticationProvider() {
        if (StringUtils.isBlank(jwtDelegateAuthenticationBasePathUrl)) {
            throw new IllegalStateException("Authentication delegate base path should be defined!");
        }

        return new JwtAuthenticationDelegateProvider(
            RestClient.builder().baseUrl(jwtDelegateAuthenticationBasePathUrl).build(),
            jwtDelegateAuthenticationBasePathLogin
        );
    }

    /**
     * Creates a custom method security expression handler.
     * This handler is used for evaluating security expressions in method annotations.
     * It supports custom expressions for checking administrator privileges.
     */
    @Bean
    static public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
        @Value("${spring.web.security.jwt.authentication.authorities.parameter-administrator:administrator}")
        String administratorAuthority
    ) {
        return new CustomMethodSecurityExpressionHandler(administratorAuthority);
    }

    /**
     * Set up Cross-Origin Resource Sharing (CORS) for JWT authentication endpoints.
     * <p>
     * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-cors-global">
     * Global CORS configuration
     * </a>
     */
    @Override
    public void addCorsMappings(final @NonNull CorsRegistry registry) {
        webSecurityCorsProperties.addCorsMappings(
            registry,
            UrlUtils.appendDoubleAsterisk(jwtAuthenticationBasePathLogin)
        );
    }
}
