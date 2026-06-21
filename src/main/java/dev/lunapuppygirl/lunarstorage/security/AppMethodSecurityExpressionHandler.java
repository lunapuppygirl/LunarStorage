package dev.lunapuppygirl.lunarstorage.security;

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class AppMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(@Nullable Authentication authentication, MethodInvocation invocation) {
        AppMethodSecurityExpressionRoot root = new AppMethodSecurityExpressionRoot(authentication);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setAuthorizationManagerFactory(getAuthorizationManagerFactory());
        return root;
    }
}
