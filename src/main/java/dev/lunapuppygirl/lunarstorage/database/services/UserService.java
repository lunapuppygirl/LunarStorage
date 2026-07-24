package dev.lunapuppygirl.lunarstorage.database.services;

import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;
import dev.lunapuppygirl.lunarstorage.database.repositories.users.User;
import dev.lunapuppygirl.lunarstorage.services.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.*;

@Service
public class UserService {
    private final Repository<User, UUID> userRepository;
    private final JwtService jwtService;

    public UserService(Repository<User, UUID> userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public @Nullable User getByUuid(UUID uuid) {
        Optional<User> user = userRepository.get(uuid);

        return user.orElse(null);
    }

    public @Nullable User getByDiscordId(long discordId) {
        List<User> user = userRepository.getAll(Integer.MAX_VALUE);

        return user.stream()
                .filter(u -> u.getDiscordId() == discordId)
                .findFirst().orElse(null);
    }

    public User getFromToken(Claims claims) {
        return getByUuid(UUID.fromString(claims.getId()));
    }

    public @Nullable User getFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        String token = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("token"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (token != null && !token.isEmpty()) {
            Claims claims = jwtService.validateToken(token);
            if (claims == null) return null;

            Date now = new Date();
            Date expiration = claims.getExpiration();

            if (expiration.before(now)) return null;

            return getFromToken(claims);
        }

        return null;
    }

    public List<User> getAll() {
        return userRepository.getAll(Integer.MAX_VALUE);
    }

    public void createUser(UUID uuid, String username, long discordId, int permissionLevel, InetAddress lastIp) {
        userRepository.create(new User(uuid, discordId, username, permissionLevel, lastIp));
    }

    public void setUsername(UUID uuid, String username) {
        User user = getByUuid(uuid);

        if (user == null) return;

        user.setDiscordUsername(username);

        userRepository.update(user);
    }

    public void setPermissionLevel(UUID uuid, int permissionLevel) {
        User user = getByUuid(uuid);

        if (user == null) return;

        user.setPermissionLevel(permissionLevel);

        userRepository.update(user);
    }

    public void deleteUser(UUID uuid) {
        User user = getByUuid(uuid);

        if (user == null) return;

        userRepository.delete(uuid);
    }
}
