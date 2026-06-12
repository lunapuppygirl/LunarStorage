package dev.lunapuppygirl.lunarstorage.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface Database extends AutoCloseable {
    void init();
    Connection getConnection() throws SQLException;
    void close() throws Exception;
}
