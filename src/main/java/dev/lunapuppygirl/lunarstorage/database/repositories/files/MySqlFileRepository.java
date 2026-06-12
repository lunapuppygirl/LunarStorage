package dev.lunapuppygirl.lunarstorage.database.repositories.files;

import dev.lunapuppygirl.lunarstorage.database.Database;
import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

@org.springframework.stereotype.Repository
public class MySqlFileRepository extends Repository<File, UUID> {
    public MySqlFileRepository(Database database, Executor executor) {
        super(database, executor);
    }

    @Override
    public Optional<File> get(UUID uuid) {
        return executeQuerySingle(
                "SELECT * FROM files WHERE uuid = ?",
                pstmt -> pstmt.setString(1, uuid.toString()),
                this::map
        );
    }

    @Override
    public void create(File entity) {
        executeUpdate(
                "INSERT INTO files VALUES (?, ?, ?, ?)",
                pstmt -> {
                    pstmt.setString(1, entity.getUuid().toString());
                    pstmt.setString(2, entity.getName());
                    pstmt.setInt(3, entity.getFolderId());
                    pstmt.setInt(4, entity.getPermissionLevel());
                }
        );
    }

    @Override
    public void update(File entity) {
        executeUpdate(
                "UPDATE files SET name = ?, folder_id = ?, permission_level = ? WHERE uuid = ?",
                pstmt -> {
                    pstmt.setString(1, entity.getName());
                    pstmt.setInt(2, entity.getFolderId());
                    pstmt.setInt(3, entity.getPermissionLevel());
                    pstmt.setString(4, entity.getUuid().toString());
                }
        );
    }

    @Override
    public void delete(UUID uuid) {
        executeUpdate(
                "DELETE FROM files WHERE uuid = ?",
                pstmt -> pstmt.setString(1, uuid.toString())
        );
    }

    @Override
    public List<File> getAll(int limit) {
        return executeQueryList(
                "SELECT * FROM files LIMIT ?",
                pstmt -> pstmt.setInt(1, limit),
                this::map
        );
    }

    @Override
    public int count() {
        return executeQuerySingle(
                "SELECT COUNT(*) FROM files",
                null,
                rs -> rs.getInt(1)
        ).orElse(0);
    }

    @Override
    public @Nullable File map(ResultSet rs) throws SQLException {
        return new File(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getInt("folder_id"),
                rs.getInt("permission_level")
        );
    }
}
