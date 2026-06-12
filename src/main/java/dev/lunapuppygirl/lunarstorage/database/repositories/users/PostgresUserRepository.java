package dev.lunapuppygirl.lunarstorage.database.repositories.users;

import dev.lunapuppygirl.lunarstorage.database.Database;
import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

@org.springframework.stereotype.Repository
public class PostgresUserRepository extends Repository<User, UUID> {
    public PostgresUserRepository(Database database, Executor executor) {
        super(database, executor);
    }

    @Override
    public Optional<User> get(UUID uuid) {
        return executeQuerySingle(
                "SELECT * FROM users WHERE uuid = ?",
                pstmt -> pstmt.setObject(1, uuid),
                this::map
        );
    }

    @Override
    public void create(User entity) {
        executeUpdate(
                "INSERT INTO users VALUES (?, ?, ?, ?, ?::inet)",
                pstmt -> {
                    pstmt.setObject(1, entity.getUuid());
                    pstmt.setLong(2, entity.getDiscordId());
                    pstmt.setString(3, entity.getDiscordUsername());
                    pstmt.setInt(4, entity.getPermissionLevel());
                    pstmt.setString(5, entity.getLastIp().getHostAddress());
                }
        );
    }

    @Override
    public void update(User entity) {
        executeUpdate(
                "UPDATE users SET discord_username = ?, permission_level = ?, last_ip = ?::inet WHERE uuid = ?",
                pstmt -> {
                    pstmt.setString(1, entity.getDiscordUsername());
                    pstmt.setInt(2, entity.getPermissionLevel());
                    pstmt.setObject(3, entity.getUuid());
                }
        );
    }

    @Override
    public void delete(UUID uuid) {
        executeUpdate(
                "DELETE FROM users WHERE uuid = ?",
                pstmt -> pstmt.setObject(1, uuid)
        );
    }

    @Override
    public List<User> getAll(int limit) {
        return executeQueryList(
                "SELECT * FROM users LIMIT ?",
                pstmt -> pstmt.setInt(1, limit),
                this::map
        );
    }

    @Override
    public int count() {
        return executeQuerySingle(
                "SELECT COUNT(*) FROM users",
                null,
                rs -> rs.getInt(1)
        ).orElse(0);
    }

    @Override
    public User map(ResultSet rs) throws SQLException {
        try {
            return new User(
                    (UUID) rs.getObject("uuid"),
                    rs.getLong("discord_id"),
                    rs.getString("discord_username"),
                    rs.getInt("permission_level"),
                    InetAddress.getByName(rs.getString("last_ip"))
            );
        } catch (UnknownHostException e) {
            logger.error("Unknown host while mapping results: ", e);
            return null;
        }
    }
}
