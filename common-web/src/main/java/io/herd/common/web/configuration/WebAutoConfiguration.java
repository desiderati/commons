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
package io.herd.common.web.configuration;

import graphql.kickstart.autoconfigure.web.servlet.GraphQLWebAutoConfiguration;
import graphql.kickstart.tools.TypeDefinitionFactory;
import graphql.schema.*;
import io.herd.common.data.jpa.configuration.JpaAutoConfiguration;
import io.herd.common.web.UrlUtils;
import io.herd.common.web.configuration.async.AsyncWebConfiguration;
import io.herd.common.web.graphql.PageableTypeDefinitionConnectionFactory;
import io.herd.common.web.rest.exception.ResponseExceptionDTOHttpMessageConverter;
import io.herd.common.web.graphql.exception.GraphQLExceptionHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Type;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springdoc.webmvc.api.MultipleOpenApiActuatorResource;
import org.springdoc.webmvc.api.MultipleOpenApiWebMvcResource;
import org.springdoc.webmvc.api.OpenApiActuatorResource;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springdoc.webmvc.ui.SwaggerConfigResource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.reflect.Method;

import static io.herd.common.web.UrlUtils.URL_PATH_SEPARATOR;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter({HibernateJpaAutoConfiguration.class, GraphQLWebAutoConfiguration.class})
@EnableWebMvc
@PropertySource("classpath:application-common-web.properties")
@ComponentScan({
    "io.herd.common.web.graphql",
    "io.herd.common.web.rest"
})
@Import({
    // Need to be auto-loaded too.
    AsyncWebConfiguration.class,
    JpaAutoConfiguration.class,
    JpaWebConfiguration.class,
    OpenApiConfiguration.class,
    RestApiClientConfiguration.class
})
public class WebAutoConfiguration implements WebMvcRegistrations, WebMvcConfigurer, RepositoryRestConfigurer {

    private final String apiBasePath;
    private final Validator validator;
    private final EntityManager entityManager;
    private CorsProperties webCorsProperties;

    @Autowired
    public WebAutoConfiguration(
        @Value("${app.api-base-path:/api}") String apiBasePath,
        @Qualifier("customValidator") Validator validator,
        ObjectProvider<EntityManager> entityManager
    ) {
        this.apiBasePath = UrlUtils.sanitize(apiBasePath);
        log.info("Configuring RESTful API base path as: {}", this.apiBasePath);

        this.validator = validator;
        this.entityManager = entityManager.getIfAvailable();
    }

    @Autowired
    public void setWebCorsProperties(
        @Lazy @Qualifier("webCorsProperties") CorsProperties webCorsProperties
    ) {
        this.webCorsProperties = webCorsProperties;
    }

    @Validated
    @Bean("webCorsProperties")
    @ConfigurationProperties("spring.web.cors")
    public CorsProperties webCorsProperties() {
        return new CorsProperties();
    }

    @Validated
    @Bean("graphqlCorsProperties")
    @ConditionalOnProperty(value = "graphql.servlet.enabled", havingValue = "true", matchIfMissing = true)
    @ConfigurationProperties("graphql.servlet.cors")
    public CorsProperties graphqlCorsProperties() {
        return new CorsProperties();
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
     * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-cors-global">
     * Global CORS configuration
     * </a>
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        webCorsProperties.addCorsMappings(registry, UrlUtils.appendDoubleAsterisk(apiBasePath));
    }

    /**
     * Ensures that all RESTs will be prefixed with {@link #defaultApiBasePath()}.
     */
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {

            @Override
            protected void registerHandlerMethod(
                @NonNull Object handler,
                @NonNull Method method,
                @NonNull RequestMappingInfo mapping
            ) {
                Class<?> beanType = method.getDeclaringClass();
                RestController restApiController = beanType.getAnnotation(RestController.class);
                if (restApiController != null && isNotOpenApiController(beanType)) {

                    String[] pathPatterns;
                    if (mapping.getPathPatternsCondition() != null) {
                        pathPatterns =
                            new PathPatternsRequestCondition(
                                PathPatternParser.defaultInstance,
                                apiBasePath
                            ).combine(mapping.getPathPatternsCondition()).getPatterns().stream()
                                .map(PathPattern::getPatternString)
                                .toArray(String[]::new);

                    } else if (mapping.getPatternsCondition() != null) {
                        pathPatterns =
                            new PatternsRequestCondition(apiBasePath)
                                .combine(mapping.getPatternsCondition())
                                .getPatterns()
                                .toArray(String[]::new);
                    } else {
                        throw new IllegalStateException(); // It should never happen!
                    }

                    mapping = mapping.mutate().paths(pathPatterns).build();
                }
                super.registerHandlerMethod(handler, method, mapping);
            }
        };
    }

    private boolean isNotOpenApiController(Class<?> beanType) {
        return !SwaggerConfigResource.class.isAssignableFrom(beanType)
            && !OpenApiWebMvcResource.class.isAssignableFrom(beanType)
            && !OpenApiActuatorResource.class.isAssignableFrom(beanType)
            && !MultipleOpenApiWebMvcResource.class.isAssignableFrom(beanType)
            && !MultipleOpenApiActuatorResource.class.isAssignableFrom(beanType);
    }

    /**
     * Ensures that all Repository RESTs will be prefixed with {@link #defaultApiBasePath()}.
     */
    @Override
    public void configureRepositoryRestConfiguration(
        RepositoryRestConfiguration repositoryRestConfiguration,
        CorsRegistry corsRegistry
    ) {
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
            RepositoryDetectionStrategy.RepositoryDetectionStrategies.ANNOTATED
        );

        // Felipe Desiderati: Spring Data Rest should allow to define the complete path
        // on annotation @RepositoryRestResource.
        // See: https://stackoverflow.com/questions/30396953/how-to-customize-spring-data-rest-to-use-a-multi-segment-path-for-a-repository-r
        // Configures the Base Path. It can be redefined using property: spring.data.rest.base-path
        if (StringUtils.isBlank(repositoryRestConfiguration.getBasePath().getPath())) {
            repositoryRestConfiguration.setBasePath(apiBasePath + URL_PATH_SEPARATOR);
        }
    }

    @Bean
    public ResponseExceptionDTOHttpMessageConverter responseExceptionDTOMessageConverter(
        @Qualifier("jacksonHttpMessageConverter") MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter
    ) {
        return new ResponseExceptionDTOHttpMessageConverter(mappingJackson2HttpMessageConverter);
    }

    @Bean
    @ConditionalOnMissingBean(GraphQLExceptionHandler.class)
    public GraphQLExceptionHandler graphQLExceptionHandler(MessageSource messageSource) {
        return new GraphQLExceptionHandler(messageSource);
    }

    @Bean
    public GraphQLScalarType voidGraphQLScalarType() {
        return GraphQLScalarType.newScalar()
            .name("Void")
            .description("Void Scalar")
            .coercing(new Coercing<Void, String>() {

                @Override
                public String serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                    return "";
                }

                @Override
                public @NotNull Void parseValue(@NotNull Object input) throws CoercingParseValueException {
                    return null;
                }

                @Override
                public @NotNull Void parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                    return null;
                }
            }).build();
    }

    @Bean
    public TypeDefinitionFactory pageableTypeDefinitionConnectionFactory() {
        return new PageableTypeDefinitionConnectionFactory();
    }
}
