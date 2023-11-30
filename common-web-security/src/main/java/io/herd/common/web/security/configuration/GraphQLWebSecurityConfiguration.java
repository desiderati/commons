package io.herd.common.web.security.configuration;

import graphql.kickstart.autoconfigure.web.OnSchemaOrSchemaProviderBean;
import graphql.kickstart.autoconfigure.web.servlet.AsyncServletProperties;
import graphql.kickstart.autoconfigure.web.servlet.GraphQLServletProperties;
import graphql.kickstart.autoconfigure.web.servlet.GraphQLWebSecurityAutoConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.concurrent.Executor;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.SERVLET)
@Conditional(OnSchemaOrSchemaProviderBean.class)
@ConditionalOnProperty(value = "graphql.servlet.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore(GraphQLWebSecurityAutoConfiguration.class)
@ConditionalOnClass({DispatcherServlet.class, DefaultAuthenticationEventPublisher.class})
@EnableConfigurationProperties({GraphQLServletProperties.class, AsyncServletProperties.class})
public class GraphQLWebSecurityConfiguration {

    private final AsyncServletProperties asyncServletProperties;

    @Bean("graphqlAsyncTaskExecutor")
    @ConditionalOnProperty(name = "spring.mvc.async.thread-context-inheritable", havingValue = "true")
    public Executor simpleGraphQLAsyncTaskExecutor(
        @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME) AsyncTaskExecutor taskExecutor
    ) {
        if (asyncServletProperties.isDelegateSecurityContext()) {
            return new DelegatingSecurityContextAsyncTaskExecutor(taskExecutor);
        }
        return null;
    }
}
