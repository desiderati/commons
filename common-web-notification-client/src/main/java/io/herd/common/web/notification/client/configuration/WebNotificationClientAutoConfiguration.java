package io.herd.common.web.notification.client.configuration;

import io.herd.common.web.client.configuration.OpenApiClientProperties;
import io.herd.common.web.notification.client.NotificationClient;
import io.openapi.client.ApiClient;
import io.openapi.client.api.BroadcastControllerApi;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.annotation.RequestScope;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "io.herd.common.web.notification.client",
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
