package dev.lunapuppygirl.lunarstorage.database.services;

import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;
import dev.lunapuppygirl.lunarstorage.database.repositories.files.File;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {
    private final Repository<File, UUID> fileRepository;

    public FileService(Repository<File, UUID> fileRepository) {
        this.fileRepository = fileRepository;
    }

    public @Nullable File getByUuid(UUID uuid) {
        Optional<File> file = fileRepository.get(uuid);

        return file.orElse(null);
    }

    public List<File> getFilesInFolder(int id) {
        List<File> list = fileRepository.getAll(Integer.MAX_VALUE);

        return list.stream()
                .filter(f -> f.getFolderId() == id)
                .toList();
    }

    public void createFile(String name, int folderId, int permissionLevel) {
        fileRepository.create(new File(UUID.randomUUID(), name, folderId, permissionLevel));
    }

    public void setName(UUID uuid, String name) {
        File file = getByUuid(uuid);

        if (file == null) return;

        file.setName(name);
        fileRepository.update(file);
    }

    public void setFolderId(UUID uuid, int folderId) {
        File file = getByUuid(uuid);

        if (file == null) return;

        file.setFolderId(folderId);
        fileRepository.update(file);
    }

    public void setPermissionLevel(UUID uuid, int permissionLevel) {
        File file = getByUuid(uuid);

        if (file == null) return;

        file.setPermissionLevel(permissionLevel);
        fileRepository.update(file);
    }

    public void deleteFile(UUID uuid) {
        File file = getByUuid(uuid);
        if (file == null) return;
        fileRepository.delete(uuid);
    }

}
