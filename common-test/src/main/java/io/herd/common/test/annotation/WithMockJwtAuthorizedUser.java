/*
 * Copyright (c) 2022 - Felipe Desiderati
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
package io.herd.common.test.annotation;

import io.herd.common.test.MockJwtAuthorizedUserSecurityContextFactory;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.Principal;

/**
 * Adds support to authorization while running tests. It will be necessary to annotate the classes
 * or methods containing the tests, indicating the client being authorized.
 *
 * @see WithMockUser
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockJwtAuthorizedUserSecurityContextFactory.class)
public @interface WithMockJwtAuthorizedUser {

    /**
     * We have defined here the bean name, which will be used to search the JWT token configured
     * inside the test context and use it as an authentication principal.
     *
     * @return The bean name registered in the test context.
     */
    String beanName() default "jwtToken";

    /**
     * It will be used to create the {@link Principal}, if JWT token not presented.
     */
    String username() default "admin";

    /**
     * Always with prefix "ROLE_".
     */
    String role() default "ROLE_ADMIN";

}
