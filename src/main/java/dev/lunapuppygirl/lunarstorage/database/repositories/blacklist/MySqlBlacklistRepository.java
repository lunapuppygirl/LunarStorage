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

    public Optional<BlacklistEntry> get(String ip) {
        List<BlacklistEntry> entries = getAll(Integer.MAX_VALUE);

        try {
            InetAddress target = InetAddress.getByName(ip);

            return entries.stream()
                    .filter(entry -> isInRange(target, entry.getIpRange()))
                    .findFirst();
        } catch (UnknownHostException e) {
            logger.error("Error while getting blacklist entry: ", e);
        }

        return Optional.empty();
    }

    private boolean isInRange(InetAddress ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] ipBytes = ip.getAddress();
            byte[] networkBytes = network.getAddress();

            if (ipBytes.length != networkBytes.length) return false;

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (ipBytes[i] != networkBytes[i]) return false;
            }
            if (remainingBits > 0) {
                int mask = 0xFF & (0xFF << (8 - remainingBits));
                return (ipBytes[fullBytes] & mask) == (networkBytes[fullBytes] & mask);
            }
            return true;
        } catch (UnknownHostException e) {
            logger.error("Error while checking for ip in range: ", e);
        }
        return true;
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
