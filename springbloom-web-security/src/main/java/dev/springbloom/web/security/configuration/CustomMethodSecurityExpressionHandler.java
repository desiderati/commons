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

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Custom implementation of the method security expression handler.
 * <p>
 * It creates a custom security expression root that includes additional expressions for checking
 * administrator privileges.
 */
public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private final String administratorAuthority;

    public CustomMethodSecurityExpressionHandler(String administratorAuthority) {
        this.administratorAuthority = administratorAuthority;
    }

    /**
     * Creates an evaluation context for evaluating security expressions.
     * <p>
     * It extracts the authentication object, creates a security expression root, and sets up
     * the evaluation context with the method information.
     *
     * @param authenticationSupplier The supplier for the authentication object
     * @param mi                     The method invocation being secured
     * @return The evaluation context for security expressions
     * @throws IllegalArgumentException if the authentication object is null
     */
    @Override
    public EvaluationContext createEvaluationContext(
        Supplier<Authentication> authenticationSupplier,
        MethodInvocation mi
    ) {
        Authentication authentication = authenticationSupplier.get();
        Assert.notNull(authentication, "Authentication object cannot be null!");
        MethodSecurityExpressionOperations root = createSecurityExpressionRoot(authentication, mi);

        Method method = AopUtils.getMostSpecificMethod(
            mi.getMethod(),
            AopProxyUtils.ultimateTargetClass(Objects.requireNonNull(mi.getThis()))
        );

        MethodBasedEvaluationContext ctx =
            new MethodBasedEvaluationContext(root, method, mi.getArguments(), getParameterNameDiscoverer());
        ctx.setBeanResolver(getBeanResolver());
        return ctx;
    }

    /**
     * Creates a custom security expression root for method security.
     * <p>
     * This method overrides the default implementation to create our custom security expression root
     * that includes additional expressions for checking administrator privileges.
     * <p>
     * It initializes the root with the authentication object and sets up various evaluators and resolvers.
     *
     * @param authentication The authentication object
     * @param invocation     The method invocation being secured
     * @return The custom security expression root
     */
    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
        Authentication authentication,
        MethodInvocation invocation
    ) {
        CustomMethodSecurityExpressionRoot root =
            new CustomMethodSecurityExpressionRoot(administratorAuthority, authentication);
        root.setThis(invocation.getThis());
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(getTrustResolver());
        root.setRoleHierarchy(getRoleHierarchy());
        root.setDefaultRolePrefix(getDefaultRolePrefix());
        return root;
    }
}
