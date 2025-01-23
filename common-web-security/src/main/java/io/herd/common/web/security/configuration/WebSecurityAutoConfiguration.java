/*
 * Copyright (c) 2024 - Felipe Desiderati
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

import graphql.kickstart.autoconfigure.web.servlet.GraphQLWebSecurityAutoConfiguration;
import io.herd.common.web.UrlUtils;
import io.herd.common.web.configuration.CorsProperties;
import io.herd.common.web.configuration.WebAutoConfiguration;
import io.herd.common.web.security.jwt.JwtServiceRestTemplateInterceptor;
import io.herd.common.web.security.jwt.JwtTokenExtractor;
import io.herd.common.web.security.jwt.authentication.*;
import io.herd.common.web.security.jwt.authorization.DefaultJwtTokenExtractor;
import io.herd.common.web.security.jwt.authorization.JwtAuthorizationFilter;
import io.herd.common.web.security.sign_request.authorization.SignRequestAuthorizationFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authorization.method.AuthorizationManagerAfterMethodInterceptor;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.web.method.annotation.CurrentSecurityContextArgumentResolver;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Collection;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.core.context.SecurityContextHolder.MODE_INHERITABLETHREADLOCAL;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@PropertySource("classpath:application-common-web-security.properties")
@AutoConfigureBefore(GraphQLWebSecurityAutoConfiguration.class)
@ComponentScan(basePackages = "io.herd.common.web.security",
    // Do not add the auto-configured classes, otherwise the auto-configuration will not work as expected.
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
)
@Import({
    WebAutoConfiguration.class,
    GraphQLWebSecurityConfiguration.class
}) // To be used with @WebMvcTest
public class WebSecurityAutoConfiguration implements WebMvcConfigurer {

    @Value("${spring.web.security.default.authentication.enabled:false}")
    private boolean defaultAuthenticationEnabled;

    @Value("${spring.web.security.jwt.authentication.enabled:false}")
    private boolean jwtAuthenticationEnabled;

    @Value("${spring.web.security.jwt.authentication.login-url:/login}")
    private String jwtAuthenticationLoginUrl;

    @Value("${spring.web.security.jwt.authentication.authorities.parameter:authorities}")
    private String jwtAuthenticationAuthoritiesParameter;

    @Value("${spring.web.security.jwt.authentication.delegation.enabled:false}")
    private boolean jwtDelegateAuthenticationEnabled;

    @Value("${spring.web.security.jwt.authentication.delegation.base-path}")
    private String jwtDelegateAuthenticationBasePath;

    @Value("${spring.web.security.jwt.authentication.delegation.login-url:/login}")
    private String jwtDelegateAuthenticationLoginUrl;

    @Value("${spring.web.security.jwt.authorization.enabled:false}")
    private boolean jwtAuthorizationEnabled;

    @Value("${spring.web.security.sign-request.authorization.enabled:false}")
    private boolean signRequestAuthorizationEnabled;

    @Value("${graphql.servlet.security.enabled:true}")
    private boolean graphqlServletSecurityEnabled;

    @Value("${graphql.servlet.mapping}")
    private String graphqlServletMapping;

    @Value("${graphql.playground.enabled:false}")
    private boolean graphqlPlaygroundEnabled;

    @Value("${graphql.voyager.enabled:false}")
    private boolean graphqlVoyagerEnabled;

    @Value("${spring.web.atmosphere.security.enabled:true}")
    private boolean atmosphereSecurityEnabled;

    @Value("${spring.web.atmosphere.url.mapping:/atmosphere}")
    private String atmosphereUrlMapping;

    @Value("${springdoc.api-docs.path:/api-docs}")
    private String springDocOpenApiPath;

    public WebSecurityAutoConfiguration(
        @Value("${graphql.servlet.async.delegate-security-context:true}")
        boolean graphqlServletAsyncDelegateSecurityContext,

        Collection<AuthorizationManagerBeforeMethodInterceptor> preAuthorizeInterceptors,
        Collection<AuthorizationManagerAfterMethodInterceptor> postAuthorizeInterceptors
    ) {
        if (graphqlServletAsyncDelegateSecurityContext) {
            SecurityContextHolder.setStrategyName(MODE_INHERITABLETHREADLOCAL);

            // As we have defined a new SecurityContextHolderStrategy, we must reconfigure
            // the method security interceptors and the argument resolvers.
            preAuthorizeInterceptors.forEach(
                it -> it.setSecurityContextHolderStrategy(SecurityContextHolder.getContextHolderStrategy())
            );
            postAuthorizeInterceptors.forEach(
                it -> it.setSecurityContextHolderStrategy(SecurityContextHolder.getContextHolderStrategy())
            );
        }
    }

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private JwtAuthorizationFilter jwtAuthorizationFilter;
    private SignRequestAuthorizationFilter signRequestAuthorizationFilter;
    private String defaultApiBasePath;
    private CorsProperties webSecurityCorsProperties;
    private CorsFilter graphQLCorsFilter;

    @Autowired
    public void setDefaultApiBasePath(String defaultApiBasePath) {
        this.defaultApiBasePath = defaultApiBasePath;
    }

    @Autowired(required = false)
    public void setJwtAuthenticationFilter(@Lazy JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Autowired(required = false)
    public void setJwtAuthorizationFilter(@Lazy JwtAuthorizationFilter jwtAuthorizationFilter) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    @Autowired(required = false)
    public void setSignRequestAuthorizationFilter(
        @Lazy SignRequestAuthorizationFilter signRequestAuthorizationFilter
    ) {
        this.signRequestAuthorizationFilter = signRequestAuthorizationFilter;
    }

    @Autowired(required = false)
    public void setWebSecurityCorsProperties(
        @Lazy @Qualifier("webSecurityCorsProperties") CorsProperties webSecurityCorsProperties
    ) {
        this.webSecurityCorsProperties = webSecurityCorsProperties;
    }

    @Autowired(required = false)
    public void setGraphQLCorsFilter(@Qualifier("corsConfigurer") CorsFilter graphQLCorsFilter) {
        this.graphQLCorsFilter = graphQLCorsFilter;
    }

    @Autowired
    public void configureArgumentResolvers(
        @Value("${graphql.servlet.async.delegate-security-context:true}")
        boolean graphqlServletAsyncDelegateSecurityContext,

        List<HandlerMethodArgumentResolver> argumentResolvers
    ) {
        if (graphqlServletAsyncDelegateSecurityContext) {
            argumentResolvers.forEach(
                it -> {
                    if (it instanceof AuthenticationPrincipalArgumentResolver) {
                        ((AuthenticationPrincipalArgumentResolver) it).setSecurityContextHolderStrategy(
                            SecurityContextHolder.getContextHolderStrategy()
                        );
                    } else if (it instanceof CurrentSecurityContextArgumentResolver) {
                        ((CurrentSecurityContextArgumentResolver) it).setSecurityContextHolderStrategy(
                            SecurityContextHolder.getContextHolderStrategy()
                        );
                    }
                }
            );
        }
    }

    @Validated
    @Bean("webSecurityCorsProperties")
    @ConfigurationProperties("spring.web.security.jwt.authentication.cors")
    public CorsProperties webSecurityCorsProperties() {
        return new CorsProperties();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationConverter.class)
    @ConditionalOnProperty(name = "spring.web.security.jwt.authentication.enabled", havingValue = "true")
    public AuthenticationConverter authenticationConverter() {
        return new DefaultJwtAuthenticationConverter(jwtDelegateAuthenticationEnabled);
    }

    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationTokenConfigurer.class)
    @ConditionalOnProperty(name = "spring.web.security.jwt.authentication.enabled", havingValue = "true")
    public JwtAuthenticationTokenConfigurer jwtAuthenticationTokenConfigurer() {
        return new DefaultJwtAuthenticationTokenConfigurer(jwtAuthenticationAuthoritiesParameter);
    }

    @Bean
    @ConditionalOnMissingBean(JwtDelegateAuthenticationProvider.class)
    @ConditionalOnExpression("${spring.web.security.jwt.authentication.enabled} and ${spring.web.security.jwt.authentication.delegation.enabled}")
    public JwtDelegateAuthenticationProvider jwtDelegateAuthenticationProvider() {
        if (StringUtils.isBlank(jwtDelegateAuthenticationBasePath)) {
            throw new IllegalStateException("Authentication delegate base path should be defined!");
        }

        return new JwtDelegateAuthenticationProvider(
            RestClient.builder().baseUrl(jwtDelegateAuthenticationBasePath).build(),
            jwtDelegateAuthenticationLoginUrl
        );
    }

    @Bean
    @RequestScope
    @ConditionalOnProperty(value = "spring.web.client.rest-template.decorate-with-auth-header", havingValue = "true")
    public JwtServiceRestTemplateInterceptor defaultRestTemplateClientJwtServiceInterceptor(
        @Qualifier("defaultRestTemplateClient") RestTemplate defaultRestTemplateClient
    ) {
        return new JwtServiceRestTemplateInterceptor(defaultRestTemplateClient);
    }

    @Bean
    @ConditionalOnMissingBean(JwtTokenExtractor.class)
    @ConditionalOnProperty(name = "spring.web.security.jwt.authorization.enabled", havingValue = "true")
    public JwtTokenExtractor<Authentication> jwtTokenExtractor() {
        return new DefaultJwtTokenExtractor(jwtAuthenticationAuthoritiesParameter);
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

    /**
     * This method allows configuration of web-based security at a resource level, based on a selection match.
     * E.g., The example below restricts the URLs that start with /admin/ to users that have ADMIN role, and
     * declares that any other URLs need to be successfully authenticated.
     * <pre>
     * public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
     *     http.authorizeRequests()
     *         .antMatchers("/admin/**").hasRole("ADMIN")
     *         .anyRequest().authenticated();
     * }
     * </pre>
     */
    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity httpSecurity,
        HandlerMappingIntrospector introspector
    ) throws Exception {
        // We don't need to enable CSRF support because our Token is invulnerable.
        // And also because with it enabled, we will not be able to call our back-end
        // from the front-end.
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        // If the GraphQL CORS filter is enabled, it must be registered before the authentication filters!
        if (graphQLCorsFilter != null) {
            httpSecurity.addFilter(graphQLCorsFilter);
        }

        // We have to enable Cross-Origin Resource Sharing.
        httpSecurity.cors(withDefaults());

        // TODO Felipe Desiderati: Permitir que possa ser personalizado esta sessão.
        // We can perform custom exception handling if authentication fails.
        // For instance, if some error occurs during the login process a 403 custom error page will be displayed!
        //httpSecurity.exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint());

        // We do not wish to enable session. Only if default authentication is enabled.
        httpSecurity.sessionManagement(sessionManagement ->
            sessionManagement.sessionCreationPolicy(
                defaultAuthenticationEnabled ?
                    SessionCreationPolicy.IF_REQUIRED :
                    SessionCreationPolicy.STATELESS
            )
        );

        // Disables page caching.
        httpSecurity.headers(headers -> headers.cacheControl(withDefaults()));

        httpSecurity.authorizeHttpRequests(authorizeHttpRequests -> {
            if (!defaultAuthenticationEnabled
                && !jwtAuthenticationEnabled
                && !jwtAuthorizationEnabled
                && !signRequestAuthorizationEnabled
            ) {
                // If none configured, it uses the default behavior.
                authorizeHttpRequests.anyRequest().permitAll();

            } else {
                // Default public endpoints. Security should not be enabled for these!

                // Default error page.
                authorizeHttpRequests
                    .requestMatchers(HttpMethod.GET, "/error")
                    .permitAll();

                // We enable all Actuator RESTs.
                authorizeHttpRequests
                    .requestMatchers(HttpMethod.GET, "/actuator/**")
                    .permitAll();

                // We enable all Open API (formally Swagger API) RESTs.
                authorizeHttpRequests.requestMatchers(HttpMethod.GET, "/swagger-resources/**").permitAll();
                authorizeHttpRequests.requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll();
                authorizeHttpRequests.requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll();
                authorizeHttpRequests.requestMatchers(HttpMethod.GET, "/webjars/**").permitAll();
                authorizeHttpRequests.requestMatchers(HttpMethod.GET, springDocOpenApiPath).permitAll();

                // It enables all calls to the public API.
                authorizeHttpRequests.requestMatchers(defaultApiBasePath + "/public/**").permitAll();

                // Notification (Atmosphere) Support.
                if (!atmosphereSecurityEnabled) {
                    // It should be used only on development environments.
                    authorizeHttpRequests.requestMatchers(UrlUtils.sanitize(atmosphereUrlMapping)).permitAll();
                    authorizeHttpRequests.requestMatchers(
                        UrlUtils.appendDoubleAsterisk(atmosphereUrlMapping)
                    ).permitAll();
                }

                // GraphQL Support.
                if (!graphqlServletSecurityEnabled) {
                    // It should be used only on development environments.
                    // As the GraphQL Url is registered by another Servlet, we must configure the security like this!
                    authorizeHttpRequests.requestMatchers(
                        new MvcRequestMatcher.Builder(introspector)
                            .servletPath(UrlUtils.sanitize(graphqlServletMapping))
                            .pattern("/**")
                    ).permitAll();
                }

                if (graphqlPlaygroundEnabled) {
                    // It should be used only on development environments.
                    // The URL mapping is not customized!
                    authorizeHttpRequests.requestMatchers("/playground").permitAll();
                    authorizeHttpRequests.requestMatchers("/playground/**").permitAll();
                    authorizeHttpRequests.requestMatchers("/vendor/playground").permitAll();
                    authorizeHttpRequests.requestMatchers("/vendor/playground/**").permitAll();
                }

                if (graphqlVoyagerEnabled) {
                    // It should be used only on development environments.
                    // The URL mapping is not customized!
                    authorizeHttpRequests.requestMatchers("/voyager").permitAll();
                    authorizeHttpRequests.requestMatchers("/voyager/**").permitAll();
                    authorizeHttpRequests.requestMatchers("/vendor/voyager").permitAll();
                    authorizeHttpRequests.requestMatchers("/vendor/voyager/**").permitAll();
                }

                if (jwtAuthenticationEnabled) {
                    // Login URL.
                    authorizeHttpRequests =
                        authorizeHttpRequests.requestMatchers(HttpMethod.POST, jwtAuthenticationLoginUrl).permitAll();
                    httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                }

                if (jwtAuthorizationEnabled) {
                    httpSecurity.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
                }

                if (signRequestAuthorizationEnabled) {
                    httpSecurity.addFilterBefore(
                        signRequestAuthorizationFilter, UsernamePasswordAuthenticationFilter.class
                    );
                }

                authorizeHttpRequests.anyRequest().authenticated();
            }
        });

        if (defaultAuthenticationEnabled) {
            // It will be enabled the filter: UsernamePasswordAuthenticationFilter
            httpSecurity.formLogin(withDefaults());
            httpSecurity.httpBasic(withDefaults());
        }

        return httpSecurity.build();
    }

    /**
     * This method is used for configuration settings that impact global security (ignore resources, set debug mode,
     * reject requests by implementing a custom firewall definition). For example, the following method would cause
     * any request that starts with /context-path/** to be ignored for authentication purposes.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    @Bean
    static public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
        @Value("${spring.web.security.jwt.authentication.authorities.parameter-administrator:administrator}")
        String jwtAuthenticationAuthoritiesAdminParameter
    ) {
        return new CustomMethodSecurityExpressionHandler(jwtAuthenticationAuthoritiesAdminParameter);
    }

    private HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }
}
