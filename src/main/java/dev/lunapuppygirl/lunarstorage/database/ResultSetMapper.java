package dev.lunapuppygirl.lunarstorage.database;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetMapper<R> {
    R map(ResultSet rs) throws SQLException;
}
