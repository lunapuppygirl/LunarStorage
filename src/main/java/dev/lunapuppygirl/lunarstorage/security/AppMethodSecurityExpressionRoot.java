package dev.lunapuppygirl.lunarstorage.security;

import dev.lunapuppygirl.lunarstorage.database.repositories.users.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class AppMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
    private Object filterObject;
    private Object returnObject;

    public AppMethodSecurityExpressionRoot(@Nullable Authentication authentication) {
        super(authentication);
    }

    public boolean hasPermissionLevel(int requiredLevel) {
        Object principal = getAuthentication().getPrincipal();
        if (principal == null) return false;
        if (!(principal instanceof User)) return false;
        return ((User) principal).getPermissionLevel() >= requiredLevel;
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public @Nullable Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public void setReturnObject(@Nullable Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public @Nullable Object getReturnObject() {
        return this.returnObject;
    }

    @Override
    public @Nullable Object getThis() {
        return this;
    }
}
