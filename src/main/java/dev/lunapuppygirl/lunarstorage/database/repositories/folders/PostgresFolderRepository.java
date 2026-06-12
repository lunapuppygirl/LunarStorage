package dev.lunapuppygirl.lunarstorage.database.repositories.folders;

import dev.lunapuppygirl.lunarstorage.database.Database;
import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

@org.springframework.stereotype.Repository
public class PostgresFolderRepository extends Repository<Folder, Integer> {
    public PostgresFolderRepository(Database database, Executor executor) {
        super(database, executor);
    }

    @Override
    public Optional<Folder> get(Integer integer) {
        return executeQuerySingle(
                "SELECT * FROM folders WHERE id = ?",
                pstmt -> pstmt.setInt(1, integer),
                this::map
        );
    }

    @Override
    public void create(Folder entity) {
        executeUpdate(
                "INSERT INTO folders VALUES (NULL, ?, ?, ?)",
                pstmt -> {
                    pstmt.setInt(1, entity.getParentId());
                    pstmt.setString(2, entity.getName());
                    pstmt.setInt(3, entity.getPermissionLevel());
                }
        );
    }

    @Override
    public void update(Folder entity) {
        executeUpdate(
                "UPDATE folders SET parent_id = ?, permission_level = ?, name = ? WHERE id = ?",
                pstmt -> {
                    pstmt.setInt(1, entity.getParentId());
                    pstmt.setInt(2, entity.getPermissionLevel());
                    pstmt.setString(3, entity.getName());
                    pstmt.setInt(4, entity.getId());
                }
        );
    }

    @Override
    public void delete(Integer integer) {
        executeUpdate(
                "DELETE FROM folders WHERE id = ?",
                pstmt ->  pstmt.setInt(1, integer)
        );
    }

    @Override
    public List<Folder> getAll(int limit) {
        return executeQueryList(
                "SELECT * FROM folders LIMIT ?",
                pstmt -> pstmt.setInt(1, limit),
                this::map
        );
    }

    @Override
    public int count() {
        return executeQuerySingle(
                "SELECT COUNT(*) FROM folders",
                null,
                rs -> rs.getInt(1)
        ).orElse(0);
    }

    @Override
    public @Nullable Folder map(ResultSet rs) throws SQLException {
        return new Folder(
                rs.getInt("id"),
                rs.getInt("parent_id"),
                rs.getString("name"),
                rs.getInt("permission_level")
        );
    }
}
