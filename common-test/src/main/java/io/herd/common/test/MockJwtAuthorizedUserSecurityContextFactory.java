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
package io.herd.common.test;

import io.herd.common.test.annotation.WithMockJwtAuthorizedUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * Adds support for authorization when running tests.
 * <p>
 * Ps.: It will be necessary to annotate the test classes as follows (only when using TestNG):
 *
 * <pre>
 * &#064;TestExecutionListeners({WithSecurityContextTestExecutionListener.class, ...})
 * public class MyTestClass extends AbstractTestNGSpringContextTests {
 *    ...
 * }</pre>
 *
 * <a href="https://docs.spring.io/spring-security/site/docs/4.0.x/reference/htmlsingle/#test-method-withsecuritycontext">
 * Spring Security Reference - @WithSecurityContext
 * </a>
 */
public class MockJwtAuthorizedUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockJwtAuthorizedUser>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockJwtAuthorizedUser jwtAuthorizedUserAnnotation) {
        Authentication auth;
        try {
            Object jwtToken =
                applicationContext.getBean(jwtAuthorizedUserAnnotation.beanName());
            auth = new UsernamePasswordAuthenticationToken(jwtToken,
                null, AuthorityUtils.createAuthorityList(jwtAuthorizedUserAnnotation.role()));
        } catch (NoSuchBeanDefinitionException ex) {
            auth = new UsernamePasswordAuthenticationToken(jwtAuthorizedUserAnnotation.username(),
                null, AuthorityUtils.createAuthorityList(jwtAuthorizedUserAnnotation.role()));
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
