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

import io.herd.common.web.security.jwt.authentication.SelfContainedJwtAuthenticationHeaderConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * It defines if a default client is created to access another service via RESTful API.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnExpression(
    "${spring.web.http.clients.enabled} and ${spring.web.http.clients.decorate-with-auth-header} and ${spring.web.security.jwt.authentication.enabled}"
)
public class HttpClientsSecurityConfiguration {

    @Autowired
    public HttpClientsSecurityConfiguration(
        @Qualifier("defaultRestTemplate") RestTemplate defaultRestTemplate,
        @Qualifier("defaultRestClient") RestClient defaultRestClient,
        ConfigurableListableBeanFactory beanFactory,
        SelfContainedJwtAuthenticationHeaderConfigurer jwtAuthenticationHeaderConfigurer
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
