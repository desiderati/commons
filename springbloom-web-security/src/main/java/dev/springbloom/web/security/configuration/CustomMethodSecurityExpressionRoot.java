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

package dev.springbloom.web.security.configuration;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/**
 * Custom implementation of {@link SecurityExpressionRoot} to support method-level security with extended expressions.
 * <p>
 * This class introduces additional security expressions, such as {@code isAdministrator()},
 * allowing fine-grained access control based on user roles.
 * <p>
 * You can use {@code isAdministrator()} within annotations like {@code @PreAuthorize} to restrict
 * access to methods that should only be executed by administrator users.
 * <pre>
 *   &#064;GetMapping
 *   &#064;PreAuthorize("isAdministrator()")
 *   public Result performAdminAction() {
 *       ...
 *   }
 * </pre>
 */
public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot
    implements MethodSecurityExpressionOperations {

    private Object filterObject;

    private Object returnObject;

    private Object target;

    private final String administratorAuthority;

    /**
     * @param administratorAuthority The parameter name for administrator authorities
     * @param authentication         The authentication object
     */
    public CustomMethodSecurityExpressionRoot(String administratorAuthority, Authentication authentication) {
        super(authentication);
        this.administratorAuthority = administratorAuthority;
    }

    /**
     * Checks if the authenticated user is an administrator.
     * <p>
     * This method examines the authorities of the authenticated user to determine
     * if they have the administrator authority.
     * <p>
     * The administrator authority is defined by the #administratorAuthority.
     *
     * @return true if the user has administrator authority, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean isAdministrator() {
        return getAuthentication().getAuthorities().stream().anyMatch(
            authority -> authority.getAuthority().equalsIgnoreCase(administratorAuthority)
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

    void setThis(Object target) {
        this.target = target;
    }

    @Override
    public Object getThis() {
        return this.target;
    }
}
