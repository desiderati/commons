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

import io.herd.common.test.annotation.WithMockJwtAuthorizedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * Adds support for authentication when running tests. It will be necessary to annotate the class
 * containing the tests as follows (only when using TestNG):
 *
 * <pre>
 * &#064;TestExecutionListeners({WithSecurityContextTestExecutionListener.class, ...})
 * public class MyTestClass extends AbstractTestNGSpringContextTests {
 *    ...
 * }</pre>
 *
 * @link https://docs.spring.io/spring-security/site/docs/4.0.x/reference/htmlsingle/#test-method-withsecuritycontext
 */
public class MockJwtAuthorizedUserSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtAuthorizedUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtAuthorizedUser jwtAuthorizedUserAnnotation) {
        Authentication auth =
            new UsernamePasswordAuthenticationToken(jwtAuthorizedUserAnnotation.username(),
                null, AuthorityUtils.createAuthorityList(jwtAuthorizedUserAnnotation.role()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
