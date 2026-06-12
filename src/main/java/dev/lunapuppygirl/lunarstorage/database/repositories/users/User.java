package dev.lunapuppygirl.lunarstorage.database.repositories.users;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;
import java.util.UUID;

@Data
@AllArgsConstructor
public class User {
    private UUID uuid;
    private long discordId;
    private String discordUsername;
    private int permissionLevel;
    private InetAddress lastIp;
}