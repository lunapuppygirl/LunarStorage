package dev.lunapuppygirl.lunarstorage.security;

import dev.lunapuppygirl.lunarstorage.database.repositories.users.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("permissions")
public class PermissionExpressions {

    public boolean hasPermissionLevel(Integer requiredLevel) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) return false;

        return user.getPermissionLevel() >= requiredLevel;
    }
}
