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

package dev.springbloom.web.notification.client.configuration;

import dev.springbloom.web.client.configuration.OpenApiClientProperties;
import dev.springbloom.web.notification.client.NotificationClient;
import io.openapi.client.ApiClient;
import io.openapi.client.api.BroadcastControllerApi;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.annotation.RequestScope;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "dev.springbloom.web.notification.client",
    // Do not add the auto-configured classes, otherwise the auto-configuration will not work as expected.
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
)
@PropertySource("classpath:notification-openapi-client.properties")
public class WebNotificationClientAutoConfiguration {

    @Bean
    @RequestScope
    public ApiClient notificationOpenApiClient(OpenApiClientProperties notificationOpenApiClientProperties) {
        ApiClient apiClient = io.openapi.client.Configuration.getDefaultApiClient();
        apiClient.setBasePath(notificationOpenApiClientProperties.getBasePath());
        return apiClient;
    }

    @Bean
    @Validated
    @ConfigurationProperties("notification.openapi.client")
    public OpenApiClientProperties notificationOpenApiClientProperties() {
        return new OpenApiClientProperties();
    }

    @Bean
    public NotificationClient notificationClient(ApiClient apiClient) {
        return new NotificationClient(new BroadcastControllerApi(apiClient));
    }
}
