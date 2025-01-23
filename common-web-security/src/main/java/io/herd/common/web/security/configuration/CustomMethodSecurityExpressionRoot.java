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
