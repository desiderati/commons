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
package io.herd.common.configuration;

import io.herd.common.exception.ResponseExceptionDTOHttpMessageConverter;
import io.herd.common.web.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Method;

import static io.herd.common.web.UrlUtils.URL_PATH_SEPARATOR;

@Slf4j
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(CorsProperties.class)
@ComponentScan("io.herd.common.exception")
@Import({DefaultAutoConfiguration.class, SwaggerConfiguration.class})
public class WebAutoConfiguration implements WebMvcConfigurer, RepositoryRestConfigurer {

    private final String apiBasePath;

    private final Validator validator;

    private final EntityManager entityManager;

    private final CorsProperties corsProperties;

    @Autowired
    public WebAutoConfiguration(@Value("${app.api-base-path:/api}") String apiBasePath,
                                @Qualifier("customValidator") Validator validator,
                                ObjectProvider<EntityManager> entityManager, CorsProperties corsProperties) {

        this.apiBasePath = UrlUtils.sanitize(apiBasePath);
        log.info("Configuring API base path as: " + this.apiBasePath);

        this.validator = validator;
        this.entityManager = entityManager.getIfAvailable();
        this.corsProperties = corsProperties;
    }

    /**
     * @return The application root path.
     */
    @Bean
    public String defaultApiBasePath() {
        return apiBasePath;
    }

    /**
     * Specify a custom Spring MessageSource for resolving validation messages, instead of relying
     * on JSR-303's default "ValidationMessages.properties" bundle in the classpath.
     */
    @Override
    public Validator getValidator() {
        return validator;
    }

    /**
     * Set up Cross-Origin Resource Sharing (CORS).
     * <p>
     * Global CORS configuration
     * https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-cors-global
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        String mapping =
            apiBasePath.equals(URL_PATH_SEPARATOR) ?
                apiBasePath + "**" :
                apiBasePath + URL_PATH_SEPARATOR + "**";

        registry.addMapping(mapping)
            .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[]{}))
            .allowedHeaders(corsProperties.getAllowedHeaders().toArray(new String[]{}))
            .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[]{}))
            .exposedHeaders(corsProperties.getExposedHeaders().toArray(new String[]{}));
    }

    /**
     * Ensures that all RESTs will be prefixed with {@link #defaultApiBasePath()}.
     * Use the versions /v1, /v2, ... within the {@link RestController}.
     */
    @Bean
    public WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
        return new WebMvcRegistrations() {

            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new RequestMappingHandlerMapping() {

                    @Override
                    protected void registerHandlerMethod(@NonNull Object handler, @NonNull Method method,
                                                         @NonNull RequestMappingInfo mapping) {
                        Class<?> beanType = method.getDeclaringClass();
                        RestController restApiController = beanType.getAnnotation(RestController.class);
                        if (restApiController != null) {
                            PatternsRequestCondition apiPattern = new PatternsRequestCondition(apiBasePath)
                                .combine(mapping.getPatternsCondition());

                            mapping = new RequestMappingInfo(mapping.getName(), apiPattern,
                                mapping.getMethodsCondition(), mapping.getParamsCondition(),
                                mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                                mapping.getProducesCondition(), mapping.getCustomCondition());
                        }
                        super.registerHandlerMethod(handler, method, mapping);
                    }
                };
            }
        };
    }

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration repositoryRestConfiguration) {
        // Forces the Spring Data Rest to return the Id of the object being handled.
        if (entityManager != null) {
            repositoryRestConfiguration.exposeIdsFor(
                entityManager.getMetamodel().getEntities().stream()
                    .map(Type::getJavaType)
                    .toArray(Class[]::new));
        }

        // Only repositories annotated with @RepositoryRestResource are exposed, unless their exported flag
        // is set to false.
        repositoryRestConfiguration.setRepositoryDetectionStrategy(
            RepositoryDetectionStrategy.RepositoryDetectionStrategies.ANNOTATED);

        // Felipe Desiderati: Spring Data Rest should allow to define the complete path
        // on annotation @RepositoryRestResource.
        // See: https://stackoverflow.com/questions/30396953/how-to-customize-spring-data-rest-to-use-a-multi-segment-path-for-a-repository-r
        // Configures the Base Path. It can be redefined using property: spring.data.rest.base-path
        if (StringUtils.isBlank(repositoryRestConfiguration.getBasePath().getPath())) {
            repositoryRestConfiguration.setBasePath(apiBasePath + URL_PATH_SEPARATOR + "v1");
        }
    }

    @Bean
    public ResponseExceptionDTOHttpMessageConverter responseExceptionDTOMessageConverter(
            @Qualifier("jacksonHttpMessageConverter") MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        return new ResponseExceptionDTOHttpMessageConverter(mappingJackson2HttpMessageConverter);
    }
}
