package dev.lunapuppygirl.lunarstorage.database.impl;

import dev.lunapuppygirl.lunarstorage.database.AbstractDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgreSqlDatabaseImpl extends AbstractDatabase {
    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlDatabaseImpl.class);

    private final String url;
    private final String username;
    private final String password;

    public PostgreSqlDatabaseImpl(String url, String user, String password) {
        this.url = url;
        this.username = user;
        this.password = password;
    }

    @Override
    public void init() {
        logger.info("Initializing PostgreSQL connection pool..");
        initPool(url, username, password, "org.postgresql.Driver", "postgres-pool");
        logger.info("PostgreSQL pool ready");

        logger.info("Setting up tables..");
        String[] queries = {
                "CREATE TABLE IF NOT EXISTS users (uuid UUID PRIMARY KEY, discord_id BIGINT UNIQUE NOT NULL, discord_username TEXT NOT NULL, permission_level INT NOT NULL DEFAULT 0, last_ip INET NOT NULL);",
                "CREATE TABLE IF NOT EXISTS folders (id SERIAL PRIMARY KEY, parent_id INT REFERENCES folders(id) ON DELETE CASCADE, name TEXT NOT NULL, permission_level INT NOT NULL DEFAULT 0, UNIQUE(parent_id, name));",
                "CREATE TABLE IF NOT EXISTS files (uuid UUID PRIMARY KEY, name TEXT, folder_id INT REFERENCES folders(id) ON DELETE SET NULL, permission_level INT NOT NULL DEFAULT 0);",
                "CREATE TABLE IF NOT EXISTS blacklist (uuid UUID PRIMARY KEY, ip_range CIDR NOT NULL, reason TEXT NOT NULL);",

                "INSERT INTO folders VALUES (1, NULL, '/', 0);"
        };

        for (String query : queries) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error while creating table '{}': {}", query, e.getMessage());
            }
        }
        logger.info("All tables were created!");
    }
}
