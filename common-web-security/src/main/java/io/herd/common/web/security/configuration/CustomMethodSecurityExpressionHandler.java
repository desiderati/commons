package io.herd.common.web.security.configuration;

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

public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private final String jwtAuthenticationAuthoritiesAdminParameter;

    public CustomMethodSecurityExpressionHandler(String jwtAuthenticationAuthoritiesAdminParameter) {
        this.jwtAuthenticationAuthoritiesAdminParameter = jwtAuthenticationAuthoritiesAdminParameter;
    }

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

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
        Authentication authentication,
        MethodInvocation invocation
    ) {
        CustomMethodSecurityExpressionRoot root =
            new CustomMethodSecurityExpressionRoot(jwtAuthenticationAuthoritiesAdminParameter, authentication);
        root.setThis(invocation.getThis());
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(getTrustResolver());
        root.setRoleHierarchy(getRoleHierarchy());
        root.setDefaultRolePrefix(getDefaultRolePrefix());
        return root;
    }
}
