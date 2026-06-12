package dev.lunapuppygirl.lunarstorage.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementPreparer {
    void prepare(PreparedStatement pstmt) throws SQLException;
}
