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
package io.herd.common.test;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.Invocation;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link SmartContextLoader} which can be used in unit tests to automatically create mocks
 * for all autowired beans that are not yet defined in the Spring Context.
 * <p>
 * Based on: <a href="https://www.foreach.be/blog/automatic-mocking-spring-beans-mockedloader?lang=nl">Automatic Mocking Spring Beans</a>
 */
@Slf4j
@SuppressWarnings("unused")
public class MockitoLoader extends SpringBootContextLoader {

    @Override
    protected SpringApplication getSpringApplication() {
        return new SpringApplicationDecorator();
    }

    /**
     * Override the beanFactory with our custom implementation.
     */
    private static class SpringApplicationDecorator extends SpringApplication {

        @Override
        protected ConfigurableApplicationContext createApplicationContext() {
            ConfigurableApplicationContext configurableApplicationContext = super.createApplicationContext();
            ContextAnnotationAutowireCandidateResolver autowireCandidateResolver =
                new ContextAnnotationAutowireCandidateResolver();
            DefaultListableBeanFactoryDecorator beanFactoryDecorator = new DefaultListableBeanFactoryDecorator();
            beanFactoryDecorator.setAutowireCandidateResolver(autowireCandidateResolver);
            ReflectionTestUtils.setField(configurableApplicationContext, "beanFactory", beanFactoryDecorator);
            return configurableApplicationContext;
        }
    }

    /**
     * It will be used to verify if some dependency wasn't satisfied and it will try to create a mock for it.
     */
    private static class DefaultListableBeanFactoryDecorator extends DefaultListableBeanFactory {

        private final transient Map<Class<?>, Object> mockedBeans = new HashMap<>();

        @Override
        public void destroySingletons() {
            super.destroySingletons();

            AtomicInteger mockedBeansWithoutInvocations = new AtomicInteger(0);
            AtomicInteger mockedBeansWithInvocations = new AtomicInteger(0);
            mockedBeans.forEach((mockedClass, mockedBean) -> {
                InvocationContainerImpl container = MockUtil.getInvocationContainer(mockedBean);
                List<Invocation> invocations = container.getInvocations();
                if (invocations.isEmpty()) {
                    mockedBeansWithoutInvocations.incrementAndGet();
                } else {
                    mockedBeansWithInvocations.incrementAndGet();
                }
                Mockito.reset(mockedBean);
            });

            log.info("*** MockedLoader stats: [" + mockedBeans.size() + "] mocked beans of which " +
                "[" + mockedBeansWithInvocations + "] with invocations and [" + mockedBeansWithoutInvocations + "] " +
                "without invocations");
            mockedBeans.clear();
        }

        @Override
        public Object resolveDependency(@NotNull DependencyDescriptor descriptor, String beanName,
                                        Set<String> autowiredBeanNames, TypeConverter typeConverter) {
            try {
                return super.resolveDependency(descriptor, beanName, autowiredBeanNames, typeConverter);

            } catch (NoSuchBeanDefinitionException noSuchBeanDefinitionException) {
                Class<?> dependencyType = descriptor.getDependencyType();
                if (Modifier.isFinal(dependencyType.getModifiers())) {
                    throw new NoSuchBeanDefinitionException(dependencyType,
                        "Cannot create an automatic mock for final type: " + dependencyType);
                }

                Object mockedBean = mockedBeans.get(dependencyType);
                if (mockedBean == null) {
                    log.info("Did not find a mocked bean for type {}! Mocking a new one...", dependencyType);
                    mockedBean = Mockito.mock(dependencyType);

                    // Just to avoid NPE while returning an Iterator.
                    if (Iterable.class.isAssignableFrom(dependencyType)) {
                       Mockito.when(((Collection<?>) mockedBean).iterator()).thenReturn(Collections.emptyIterator());
                    }

                    // We could actually also try to instantiate the Impl if we feel the need.
                    mockedBeans.put(dependencyType, mockedBean);
                } else {
                    log.debug("Returning mocked bean for type {}", dependencyType);
                }
                return mockedBean;
            }
        }
    }
}
