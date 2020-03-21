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

import io.herd.common.security.jwt.JwtTokenExtractor;
import io.herd.common.security.jwt.authentication.JwtAuthenticationFilter;
import io.herd.common.security.jwt.authentication.JwtAuthenticationTokenConfigurer;
import io.herd.common.security.jwt.authentication.JwtDefaultAuthenticationConverter;
import io.herd.common.security.jwt.authentication.JwtDefaultAuthenticationTokenConfigurer;
import io.herd.common.security.jwt.authorization.JwtAuthorizationFilter;
import io.herd.common.security.jwt.authorization.JwtDefaultTokenExtractor;
import io.herd.common.security.sign_request.authorization.SignRequestAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
@SpringBootConfiguration
@EnableAutoConfiguration
@PropertySource("classpath:application-common-security.properties")
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

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

    @Autowired(required = false) // Required = false does not work with constructors!
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired(required = false) // Required = false does not work with constructors!
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Autowired(required = false) // Required = false does not work with constructors!
    private SignRequestAuthorizationFilter signRequestAuthorizationFilter;

    /**
     * By default we leave all requests open.
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF because our Token is invulnerable. And also because with it enabled,
        // we will not be able to call our back-end from the front-end.
        httpSecurity.csrf().disable()

            // We enable Cross-Origin Resource Sharing.
            .cors()

            // We can perform custom exception handling if authentication fails.
            //.and().exceptionHandling().authenticationEntryPoint(http403ForbiddenEntryPoint())

            // We do not wish to enable session. Only if default authentication is enabled.
            .and().sessionManagement().sessionCreationPolicy(
                defaultAuthenticationEnabled ? SessionCreationPolicy.IF_REQUIRED : SessionCreationPolicy.STATELESS)

            // Disables page caching.
            .and().headers().cacheControl();

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequests =
            httpSecurity.authorizeRequests();

        // Default public endpoints. Security should not be enabled for these!
        authorizeRequests = authorizeRequests
            // We enable all Swagger RESTs.
            .antMatchers("/swagger-resources/**").permitAll()
            .antMatchers("/swagger-ui/**").permitAll()
            .antMatchers("/swagger-ui.html").permitAll()
            .antMatchers("/webjars/springfox-swagger-ui/**").permitAll()

            // We enable all Actuator RESTs.
            .antMatchers("/actuator/**").permitAll()

            // It enables all calls to the public API.
            // FIXME Felipe Desiderati: Remove the version from here in the future!
            .antMatchers("/api/v1/public/**").permitAll();

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
                authorizeRequests.antMatchers("/").hasAnyRole("ADMIN");
                authorizeRequests.anyRequest().authenticated()
                    .and().formLogin()
                    .and().httpBasic();
            } else {
                // All other requests will be authenticated.
                authorizeRequests.anyRequest().authenticated();
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationConverter.class)
    @ConditionalOnProperty(name = "security.jwt.authentication.enabled", havingValue = "true")
    public AuthenticationConverter authenticationConverter() {
        return new JwtDefaultAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationTokenConfigurer.class)
    @ConditionalOnProperty(name = "security.jwt.authentication.enabled", havingValue = "true")
    public JwtAuthenticationTokenConfigurer jwtAuthenticationTokenConfigurer() {
        return new JwtDefaultAuthenticationTokenConfigurer();
    }

    @Bean
    @ConditionalOnMissingBean(JwtTokenExtractor.class)
    @ConditionalOnProperty(name = "security.jwt.authorization.enabled", havingValue = "true")
    public JwtTokenExtractor<Authentication> jwtTokenExtractor() {
        return new JwtDefaultTokenExtractor();
    }

    @Override
    public void configure(WebSecurity web) {
        web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    protected HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }
}
