package dev.lunapuppygirl.lunarstorage.database.repositories;

import dev.lunapuppygirl.lunarstorage.database.Database;
import dev.lunapuppygirl.lunarstorage.database.ResultSetMapper;
import dev.lunapuppygirl.lunarstorage.database.StatementPreparer;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@org.springframework.stereotype.Repository
public abstract class Repository<T, ID> {

    protected final Database database;
    protected final Executor executor;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public Repository(Database database, Executor executor) {
        this.database = database;
        this.executor = executor;
    }

    public abstract Optional<T> get(ID id);
    public abstract void create(T entity);
    public abstract void update(T entity);
    public abstract void delete(ID id);
    public abstract List<T> getAll(int limit);
    public abstract int count();
    public abstract @Nullable T map(ResultSet rs) throws SQLException;

    public CompletableFuture<Optional<T>> getAsync(ID id) {
        return CompletableFuture.supplyAsync(() -> get(id), executor);
    }
    public CompletableFuture<Void> createAsync(T entity) {
        return CompletableFuture.runAsync(() -> create(entity), executor);
    }
    public CompletableFuture<Void> updateAsync(T entity) {
        return CompletableFuture.runAsync(() -> update(entity), executor);
    }
    public CompletableFuture<Void> deleteAsync(ID id) {
        return CompletableFuture.runAsync(() -> delete(id), executor);
    }

    protected void executeUpdate(String sql, @Nullable StatementPreparer preparer) {
        try (Connection conn = database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (preparer != null) preparer.prepare(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Update failed: " + sql, e);
        }
    }

    protected <R> Optional<R> executeQuerySingle(String sql, @Nullable StatementPreparer preparer, ResultSetMapper<R> mapper) {
        try (Connection conn = database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (preparer != null) preparer.prepare(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? Optional.of(mapper.map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Query failed: " + sql, e);
        }
    }

    protected <R> List<R> executeQueryList(String sql, @Nullable StatementPreparer preparer, ResultSetMapper<R> mapper) {
        List<R> results = new ArrayList<>();
        try (Connection conn = database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (preparer != null) preparer.prepare(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) results.add(mapper.map(rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Query failed: " + sql, e);
        }
        return results;
    }
}