package dev.lunapuppygirl.lunarstorage.database.repositories.folders;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Folder {
    private int id;
    private int parentId;
    String name;
    private int permissionLevel;
}
