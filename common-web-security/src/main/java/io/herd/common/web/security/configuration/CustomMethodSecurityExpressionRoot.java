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

package io.herd.common.web.security.configuration;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot
    implements MethodSecurityExpressionOperations {

    private Object filterObject;

    private Object returnObject;

    private Object target;

    private final String jwtAuthenticationAuthoritiesAdminParameter;

    public CustomMethodSecurityExpressionRoot(
        String jwtAuthenticationAuthoritiesAdminParameter,
        Authentication authentication
    ) {
        super(authentication);
        this.jwtAuthenticationAuthoritiesAdminParameter = jwtAuthenticationAuthoritiesAdminParameter;
    }

    /**
     * It checks if the authenticated user is an administrator or not.
     */
    @SuppressWarnings("unused")
    public boolean isAdministrator() {
        return getAuthentication().getAuthorities().stream().anyMatch(
            authority -> authority.getAuthority().equalsIgnoreCase(jwtAuthenticationAuthoritiesAdminParameter)
        );
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    /**
     * Sets the "this" property for use in expressions. Typically, this will be the "this"
     * property of the {@code JoinPoint} representing the method invocation which is being
     * protected.
     *
     * @param target the target object on which the method in is being invoked.
     */
    void setThis(Object target) {
        this.target = target;
    }

    @Override
    public Object getThis() {
        return this.target;
    }
}
