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
package io.herd.common.security.configuration;

import io.herd.common.web.configuration.WebAutoConfiguration;
import io.herd.common.security.jwt.authentication.JwtAuthenticationFilter;
import io.herd.common.security.jwt.authorization.JwtAuthorizationFilter;
import io.herd.common.security.sign_request.authorization.SignRequestAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

/**
 * If you have defined your custom {@link WebSecurityConfigurerAdapter}, all basic configurations
 * will be disabled and you will have to reconfigure the endpoints permissions by yourself.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
@Import(WebAutoConfiguration.class)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfigurerAdapterAutoConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${security.default.authentication.enabled:false}")
    private boolean defaultAuthenticationEnabled;

    @Value("${security.jwt.authentication.enabled:false}")
    private boolean jwtAuthenticationEnabled;

    @Value("${security.jwt.authentication.login-url:/api/v1/login}")
    private String jwtLoginUrl;

    @Value("${security.jwt.authorization.enabled:false}")
    private boolean jwtAuthorizationEnabled;

    @Value("${security.sign-request.authorization.enabled:false}")
    private boolean signRequestAuthorizationEnabled;

    @Value("${springfox.documentation.swagger.v2.path}")
    private String springFoxSwaggerPath;

    @Value("${springfox.documentation.open-api.v3.path}")
    private String springFoxOpenApiPath;

    @Autowired(required = false) // Prevent circular dependency.
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired(required = false) // Prevent circular dependency.
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Autowired(required = false) // Prevent circular dependency.
    private SignRequestAuthorizationFilter signRequestAuthorizationFilter;

    private final String defaultApiBasePath;

    @Autowired
    public WebSecurityConfigurerAdapterAutoConfiguration(String defaultApiBasePath) {
        this.defaultApiBasePath = defaultApiBasePath;
    }

    /**
     * This method allows configuration of web-based security at a resource level, based on a selection match.
     * E.g. The example below restricts the URLs that start with /admin/ to users that have ADMIN role, and
     * declares that any other URLs need to be successfully authenticated.
     * <pre>
     * protected void configure(HttpSecurity http) throws Exception {
     *     http.authorizeRequests()
     *         .antMatchers("/admin/**").hasRole("ADMIN")
     *         .anyRequest().authenticated();
     * }
     * </pre>
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // We don't need to enable CSRF support because our Token is invulnerable.
        // And also because with it enabled, we will not be able to call our back-end
        // from the front-end.
        httpSecurity.csrf().disable()

            // We enable Cross-Origin Resource Sharing.
            .cors()

            // We can perform custom exception handling if authentication fails.
            //.and().exceptionHandling().authenticationEntryPoint(http403ForbiddenEntryPoint())

            // We do not wish to enable session. Only if default authentication is enabled.
            .and().sessionManagement().sessionCreationPolicy(
                defaultAuthenticationEnabled ?
                    SessionCreationPolicy.IF_REQUIRED :
                    SessionCreationPolicy.STATELESS)

            // Disables page caching.
            .and().headers().cacheControl();

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequests =
            httpSecurity.authorizeRequests();
        if (!defaultAuthenticationEnabled && !jwtAuthenticationEnabled && !jwtAuthorizationEnabled
                && !signRequestAuthorizationEnabled) {
            // If none configured, it uses the default behavior.
            authorizeRequests.anyRequest().permitAll();

        } else {
            if (jwtAuthenticationEnabled) {
                // Login API.
                authorizeRequests = authorizeRequests.antMatchers(HttpMethod.POST, jwtLoginUrl).permitAll();
                httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            }

            if (jwtAuthorizationEnabled) {
                httpSecurity.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
            }

            if (signRequestAuthorizationEnabled) {
                httpSecurity.addFilterBefore(signRequestAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
            }

            if (defaultAuthenticationEnabled) {
                authorizeRequests.anyRequest().authenticated()
                    .and().formLogin()
                    .and().httpBasic();
            } else {
                // All other requests will be authenticated.
                authorizeRequests.anyRequest().authenticated();
            }
        }
    }

    /**
     * This method is used for configuration settings that impact global security (ignore resources, set debug mode,
     * reject requests by implementing a custom firewall definition). For example, the following method would cause
     * any request that starts with /context-path/** to be ignored for authentication purposes.
     */
    @Override
    public void configure(WebSecurity web) {
        web.httpFirewall(allowUrlEncodedSlashHttpFirewall());

        // Default public endpoints. Security should not be enabled for these!
        web.ignoring()
            // Default error page.
            .antMatchers("/error")

            // We enable all Actuator RESTs.
            .antMatchers(defaultApiBasePath + "/actuator/**")

            // We enable all Swagger RESTs.
            .antMatchers("/swagger-resources/**")
            .antMatchers("/swagger-ui/**")
            .antMatchers("/webjars/**")
            .antMatchers(springFoxSwaggerPath)
            .antMatchers(springFoxOpenApiPath)

            // It enables all calls to the public API.
            .antMatchers(defaultApiBasePath + "/public/**")

            // GraphQL Support.
            .antMatchers("/vendor/graphiql/**")
            .antMatchers(defaultApiBasePath + "/graphiql");
    }

    private HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }
}
