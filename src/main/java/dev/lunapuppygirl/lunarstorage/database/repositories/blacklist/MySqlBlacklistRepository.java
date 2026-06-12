package dev.lunapuppygirl.lunarstorage.database.repositories.blacklist;

import dev.lunapuppygirl.lunarstorage.database.Database;
import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

@org.springframework.stereotype.Repository
public class MySqlBlacklistRepository extends Repository<BlacklistEntry, UUID> {
    public MySqlBlacklistRepository(Database database, Executor executor) {
        super(database, executor);
    }

    @Override
    public Optional<BlacklistEntry> get(UUID uuid) {
        return executeQuerySingle(
                "SELECT * FROM blacklist WHERE uuid = ?",
                pstmt -> pstmt.setString(1, uuid.toString()),
                this::map
        );
    }

    @Override
    public void create(BlacklistEntry entity) {
        executeUpdate(
                "INSERT INTO blacklist VALUES (?, ?, ?)",
                pstmt -> {
                    pstmt.setString(1, entity.getUuid().toString());
                    pstmt.setString(2, entity.getIpRange());
                    pstmt.setString(3, entity.getReason());
                }
        );
    }

    @Override
    public void update(BlacklistEntry entity) {
        executeUpdate(
                "UPDATE blacklist SET reason = ? WHERE uuid = ?",
                pstmt -> {
                    pstmt.setString(1, entity.getReason());
                    pstmt.setString(2, entity.getUuid().toString());
                }
        );
    }

    @Override
    public void delete(UUID uuid) {
        executeUpdate(
                "DELETE FROM blacklist WHERE uuid = ?",
                pstmt -> pstmt.setObject(1, uuid)
        );
    }

    @Override
    public List<BlacklistEntry> getAll(int limit) {
        return executeQueryList(
                "SELECT * FROM blacklist LIMIT ?",
                pstmt -> pstmt.setInt(1, limit),
                this::map
        );
    }

    @Override
    public int count() {
        return executeQuerySingle(
                "SELECT COUNT(*) FROM blacklist",
                null,
                rs -> rs.getInt(1)
        ).orElse(0);
    }

    @Override
    public @Nullable BlacklistEntry map(ResultSet rs) throws SQLException {
        return new BlacklistEntry(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("ip_range"),
                rs.getString("reason")
        );
    }
}
