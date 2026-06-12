package dev.lunapuppygirl.lunarstorage.database;

import dev.lunapuppygirl.lunarstorage.database.impl.MySqlDatabaseImpl;
import dev.lunapuppygirl.lunarstorage.database.impl.PostgreSqlDatabaseImpl;
import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;
import dev.lunapuppygirl.lunarstorage.database.repositories.blacklist.BlacklistEntry;
import dev.lunapuppygirl.lunarstorage.database.repositories.blacklist.MySqlBlacklistRepository;
import dev.lunapuppygirl.lunarstorage.database.repositories.blacklist.PostgresBlacklistRepository;
import dev.lunapuppygirl.lunarstorage.database.repositories.files.File;
import dev.lunapuppygirl.lunarstorage.database.repositories.files.MySqlFileRepository;
import dev.lunapuppygirl.lunarstorage.database.repositories.files.PostgresFileRepository;
import dev.lunapuppygirl.lunarstorage.database.repositories.folders.Folder;
import dev.lunapuppygirl.lunarstorage.database.repositories.folders.MySqlFolderRepository;
import dev.lunapuppygirl.lunarstorage.database.repositories.folders.PostgresFolderRepository;
import dev.lunapuppygirl.lunarstorage.database.repositories.users.MySqlUserRepository;
import dev.lunapuppygirl.lunarstorage.database.repositories.users.PostgresUserRepository;
import dev.lunapuppygirl.lunarstorage.database.repositories.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.UUID;
import java.util.concurrent.Executor;

@Configuration
public class DatabaseConfig {
    private final String databaseType;
    private final String username;
    private final String password;
    private final String url;
    private final Executor executor;

    public DatabaseConfig(
            @Value("${app.database.type:postgresql}") String databaseType,
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            Executor executor) {
        this.databaseType = databaseType;
        this.url = url;
        this.username = username;
        this.password = password;
        this.executor = executor;
    }

    private <T> T pick(T postgres, T mysql) {
        return switch (databaseType.toLowerCase()) {
            case "postgresql" -> postgres;
            case "mysql"      -> mysql;
            default -> throw new IllegalArgumentException(
                    "Unknown database type '%s'. Expected 'postgresql' or 'mysql'".formatted(databaseType));
        };
    }


    @Bean
    public Database database() {
        Database db = pick(
                new PostgreSqlDatabaseImpl(url, username, password),
                new MySqlDatabaseImpl(url, username, password)
        );
        db.init();
        return db;
    }

    @Bean
    public Repository<User, UUID> userRepository(Database database) {
        return pick(
                new PostgresUserRepository(database, executor),
                new MySqlUserRepository(database, executor)
        );
    }

    @Bean
    public Repository<Folder, Integer> folderRepository(Database database) {
        return pick(
                new PostgresFolderRepository(database, executor),
                new MySqlFolderRepository(database, executor)
        );
    }

    @Bean
    public Repository<File, UUID> fileRepository(Database database) {
        return pick(
                new PostgresFileRepository(database, executor),
                new MySqlFileRepository(database, executor)
        );
    }

    @Bean
    public Repository<BlacklistEntry, UUID> blacklistRepository(Database database) {
        return pick(
                new PostgresBlacklistRepository(database, executor),
                new MySqlBlacklistRepository(database, executor)
        );
    }
}
