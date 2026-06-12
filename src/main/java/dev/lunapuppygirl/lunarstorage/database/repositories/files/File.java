package dev.lunapuppygirl.lunarstorage.database.repositories.files;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class File {
    private UUID uuid;
    private String name;
    private int folderId;
    private int permissionLevel;
}
