/*
 * Copyright (c) 2021 - Felipe Desiderati
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

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.Callable;

@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AsyncProperties.class)
@ConditionalOnProperty(name = "spring.async.enabled", havingValue = "true")
public class AsyncConfiguration implements AsyncConfigurer {

    private final AsyncProperties asyncProperties;

    @Autowired
    public AsyncConfiguration(AsyncProperties asyncProperties) {
        this.asyncProperties = asyncProperties;
    }

    @Bean(name = "defaultAsyncTaskExecutor")
    public AsyncTaskExecutor defaultAsyncTaskExecutor() {
        log.info("Creating default asynchronous task executor...");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("Async-");
        executor.setCorePoolSize(asyncProperties.getInitialPoolSize());
        executor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncProperties.getQueueCapacity());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    /** Configure async support for Spring MVC. */
    @Bean
    public WebMvcConfigurer webMvcConfigurerConfigurer(
        @Qualifier("defaultAsyncTaskExecutor") AsyncTaskExecutor taskExecutor,
        CallableProcessingInterceptor callableProcessingInterceptor
    ) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(@NotNull AsyncSupportConfigurer configurer) {
                log.info("Configuring Spring MVC with asynchronous task executor...");
                configurer.setDefaultTimeout(asyncProperties.getTaskTimeout()).setTaskExecutor(taskExecutor);
                configurer.registerCallableInterceptors(callableProcessingInterceptor);
                WebMvcConfigurer.super.configureAsyncSupport(configurer);
            }
        };
    }

    @Bean
    public CallableProcessingInterceptor callableProcessingInterceptor() {
        return new TimeoutCallableProcessingInterceptor() {
            @Override
            public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {
                log.error(
                    "Timeout while executing asynchronous request: {}",
                    ((HttpServletRequest) request.getNativeRequest()).getRequestURI()
                );
                return super.handleTimeout(request, task);
            }
        };
    }
}

