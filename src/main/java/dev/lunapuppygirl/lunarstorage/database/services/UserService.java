package dev.lunapuppygirl.lunarstorage.database.services;

import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;
import dev.lunapuppygirl.lunarstorage.database.repositories.users.User;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final Repository<User, UUID> userRepository;

    public UserService(Repository<User, UUID> userRepository) {
        this.userRepository = userRepository;
    }

    public @Nullable User getByUuid(UUID uuid) {
        Optional<User> user = userRepository.get(uuid);

        return user.orElse(null);
    }

    public User getFromToken(Claims claims) {
        return getByUuid(UUID.fromString(claims.getId()));
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
