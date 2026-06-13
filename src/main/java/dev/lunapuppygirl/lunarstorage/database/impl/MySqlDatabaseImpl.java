package dev.lunapuppygirl.lunarstorage.database.impl;

import dev.lunapuppygirl.lunarstorage.database.AbstractDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySqlDatabaseImpl extends AbstractDatabase {
    private static final Logger logger = LoggerFactory.getLogger(MySqlDatabaseImpl.class);

    private final String url;
    private final String username;
    private final String password;

    public MySqlDatabaseImpl(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void init() {
        logger.info("Initializing MySQL connection pool..");
        initPool(url, username, password, "com.mysql.cj.jdbc.Driver", "mysql-pool");
        logger.info("MySQL pool ready");

        logger.info("Setting up tables..");
        String[] queries = {
                "CREATE TABLE IF NOT EXISTS users (uuid CHAR(36) PRIMARY KEY, discord_id BIGINT UNIQUE NOT NULL, discord_username VARCHAR(255) NOT NULL, permission_level INT NOT NULL DEFAULT 0, last_ip VARCHAR(45) NOT NULL);",
                "CREATE TABLE IF NOT EXISTS folders (id INT AUTO_INCREMENT PRIMARY KEY, parent_id INT REFERENCES folders(id) ON DELETE CASCADE, name VARCHAR(255) NOT NULL, perm_level INT NOT NULL DEFAULT 0, UNIQUE(parent_id, name));",
                "CREATE TABLE IF NOT EXISTS files (uuid CHAR(36) PRIMARY KEY, name VARCHAR(255), folder_id INT REFERENCES folders(id) ON DELETE SET NULL, perm_level INT NOT NULL DEFAULT 0);",
                "CREATE TABLE IF NOT EXISTS blacklist (uuid CHAR(36) AUTO_INCREMENT PRIMARY KEY, ip_range VARCHAR(50) NOT NULL, reason TEXT NOT NULL);",

                "INSERT IGNORE INTO folders VALUES (1, NULL, '/', 0);"
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
