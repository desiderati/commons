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
package dev.springbloom.web.configuration.async;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Callable;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.async.enabled", havingValue = "true")
public class AsyncWebConfiguration {

    /**
     * Configure async support for Spring MVC.
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurerConfigurer(CallableProcessingInterceptor callableProcessingInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(@NotNull AsyncSupportConfigurer configurer) {
                log.info("Configuring Spring MVC with custom CallableProcessingInterceptor...");
                configurer.registerCallableInterceptors(callableProcessingInterceptor);
            }
        };
    }

    @Bean
    public CallableProcessingInterceptor callableProcessingInterceptor() {
        return new TimeoutCallableProcessingInterceptor() {
            @Override
            public <T> @NotNull Object handleTimeout(
                @NotNull NativeWebRequest request,
                @NotNull Callable<T> task
            ) throws Exception {
                log.error(
                    "Timeout while executing asynchronous request: {}",
                    ((HttpServletRequest) request.getNativeRequest()).getRequestURI()
                );
                return super.handleTimeout(request, task);
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "spring.mvc.async.context-propagation-mode", havingValue = "NON_INHERITABLE")
    public AsyncContextAwareTaskDecorator asyncContextAwareTaskDecorator() {
        log.info("Creating asynchronous context task decorator...");
        return new AsyncContextAwareTaskDecorator();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.mvc.async.context-propagation-mode", havingValue = "INHERITABLE")
    public RequestContextFilter requestContextFilter() {
        RequestContextFilter requestContextFilter = new OrderedRequestContextFilter();
        requestContextFilter.setThreadContextInheritable(true);
        return requestContextFilter;
    }
}
