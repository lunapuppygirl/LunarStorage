package dev.lunapuppygirl.lunarstorage.database.services;

import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;
import dev.lunapuppygirl.lunarstorage.database.repositories.folders.Folder;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FolderService {
    private final Repository<Folder, Integer> folderRepository;

    public FolderService(Repository<Folder, Integer> folderRepository) {
        this.folderRepository = folderRepository;
    }

    public @Nullable Folder getById(int id) {
        Optional<Folder> folder = folderRepository.get(id);
        return folder.orElse(null);
    }

    public @Nullable Folder getByName(String name) {
        List<Folder> folders = folderRepository.getAll(Integer.MAX_VALUE);

        return folders.stream()
                .filter(f -> f.getName().equals(name))
                .findFirst().orElse(null);
    }

    public List<Folder> getAll() {
        return folderRepository.getAll(Integer.MAX_VALUE);
    }

    public List<Folder> getFoldersInFolder(int id) {
        List<Folder> folders = folderRepository.getAll(Integer.MAX_VALUE);

        return folders.stream()
                .filter(f -> f.getParentId() == id)
                .filter(f -> f.getId() != 0)
                .toList();
    }

    public void createFolder(String name, int parentId, int permissionLevel) {
        folderRepository.create(new Folder(-1, parentId, name, permissionLevel));
    }

    public void deleteFolder(int id) {
        folderRepository.delete(id);
    }

    public void setName(int id, String name) {
        Folder folder = getById(id);

        if (folder == null) return;

        folder.setName(name);
        folderRepository.update(folder);
    }

    public void setPermissionLevel(int id, int permissionLevel) {
        Folder folder = getById(id);

        if (folder == null) return;

        folder.setPermissionLevel(permissionLevel);
        folderRepository.update(folder);
    }
}
