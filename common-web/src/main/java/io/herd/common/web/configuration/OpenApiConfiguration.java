/*
 * Copyright (c) 2023 - Felipe Desiderati
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
package io.herd.common.web.configuration;

import io.herd.common.web.UrlUtils;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.configuration.SpringDocDataRestConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Use this configuration whenever you need to expose application controllers ({@link RestController})
 * via Open API (formally Swagger API).
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "springdoc.enabled", havingValue = "true")
@EnableConfigurationProperties(SpringDocProperties.class)
@Import({
    // Support to Spring Data Rest with Open API
    RepositoryRestMvcAutoConfiguration.class,
    SpringDocDataRestConfiguration.class
})
public class OpenApiConfiguration {

    @Value("${springdoc.package-to-scan:}")
    private String packagesToScan;

    @Value("${springdoc.paths-to-expose:/v.*,/public/v.*}")
    private String pathsToExpose;

    @Value("${server.servlet.context-path:/}")
    private String servletContextPath;

    private final String defaultApiBasePath;

    private final SpringDocProperties springDocProperties;

    @Autowired
    public OpenApiConfiguration(
        String defaultApiBasePath,
        SpringDocProperties springDocProperties
    ) {
        this.defaultApiBasePath = defaultApiBasePath;
        this.springDocProperties = springDocProperties;
    }

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server().url(computePathToExpose(""));
        return new OpenAPI()
            .info(
                new Info().title(springDocProperties.getTitle())
                    .description(springDocProperties.getDescription())
                    .version(springDocProperties.getVersion())
                    .license(
                        new License()
                            .name(springDocProperties.getLicenseName())
                            .url(springDocProperties.getLicenseUrl())
                    )
            )
            .servers(List.of(server))
            .externalDocs(
                new ExternalDocumentation()
                    .description(springDocProperties.getWikiDescription())
                    .url(springDocProperties.getWikiUrl())
            );
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        // Group name should not be changed!
        GroupedOpenApi.Builder builder = GroupedOpenApi.builder().group("swagger-config");

        if (StringUtils.isNotBlank(packagesToScan)) {
            builder.packagesToScan(packagesToScan.split(","));
        }

        if (StringUtils.isNotBlank(pathsToExpose)) {
            builder.pathsToMatch(
                Arrays.stream(pathsToExpose.split(",")).map(this::computePathToExpose).toArray(String[]::new)
            );
        }

        return builder.build();
    }

    private String computePathToExpose(String pathToExpose) {
        pathToExpose = UrlUtils.sanitize(servletContextPath)
            + UrlUtils.sanitize(defaultApiBasePath)
            + UrlUtils.sanitize(pathToExpose);
        return UrlUtils.sanitize(pathToExpose);
    }
}
