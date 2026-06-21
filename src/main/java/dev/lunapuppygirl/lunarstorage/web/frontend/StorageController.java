package dev.lunapuppygirl.lunarstorage.web.frontend;

import dev.lunapuppygirl.lunarstorage.database.repositories.files.File;
import dev.lunapuppygirl.lunarstorage.database.repositories.folders.Folder;
import dev.lunapuppygirl.lunarstorage.database.repositories.users.User;
import dev.lunapuppygirl.lunarstorage.database.services.FileService;
import dev.lunapuppygirl.lunarstorage.database.services.FolderService;
import dev.lunapuppygirl.lunarstorage.database.services.UserService;
import dev.lunapuppygirl.lunarstorage.managers.JsonFileManager;
import dev.lunapuppygirl.lunarstorage.services.PowService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Contract;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/storage")
class StorageController {
    private final PowService powService;
    private final FolderService folderService;
    private final FileService fileService;
    private final UserService userService;
    private final JsonFileManager jsonFileManager;

    StorageController(PowService powService, FolderService folderService, FileService fileService, UserService userService, JsonFileManager jsonFileManager) {
        this.powService = powService;
        this.folderService = folderService;
        this.fileService = fileService;
        this.userService = userService;
        this.jsonFileManager = jsonFileManager;
    }

    @GetMapping(path = {"/", ""})
    public String getHomeFolder(Model model, HttpServletRequest request) {
        model.addAttribute("year", Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR));

        if (!powService.isAfterVerification(request)) return "verification";

        User user = userService.getFromRequest(request);

        Folder folder = folderService.getById(0);
        assert folder != null;

        //  File  Accessible?
        Map<File, Boolean> files = getFilesInFolder(folder, user);
        Map<Folder, Boolean> folders = getFoldersInFolder(folder, user);

        model.addAttribute("files", files);
        model.addAttribute("folders", folders);
        model.addAttribute("path", folder.getName());
        model.addAttribute("user", user);
        model.addAttribute("minDashboardLevel", jsonFileManager.getInt("admin.dashboard.required_level", jsonFileManager.getConfigFile(), 1000));

        return "storage";
    }

    @GetMapping("/{*path}")
    public String getFolder(@PathVariable String path, Model model, HttpServletRequest request) {
        model.addAttribute("year", Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR));

        if (!powService.isAfterVerification(request)) return "verification";

        User user = userService.getFromRequest(request);

        List<Folder> all = folderService.getAll();

        Folder folder = resolvePath(path, all);

        if (folder == null) {
            model.addAttribute("code", 404);
            return "error";
        }

        //  File  Accessible?
        Map<File, Boolean> files = getFilesInFolder(folder, user);
        Map<Folder, Boolean> folders = getFoldersInFolder(folder, user);

        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        String parentPath = cleanPath.contains("/") ? cleanPath.substring(0, cleanPath.lastIndexOf("/")) : "";

        model.addAttribute("path", cleanPath);
        model.addAttribute("parentPath", parentPath);

        model.addAttribute("files", files);
        model.addAttribute("folders", folders);
        model.addAttribute("user", user);

        return "storage";
    }

    private Map<File, Boolean> getFilesInFolder(Folder folder, @Nullable User user) {
        Map<File, Boolean> files = new HashMap<>();

        // if user != null and file perm level > user perm level, set accessible to false
        // if user == null and file perm level > 0, set accessible to false
        // if file perm level == 0, set accessible to true
        for (File f : fileService.getFilesInFolder(folder.getId())) {
            if (user != null) {
                if (f.getPermissionLevel() > user.getPermissionLevel()) {
                    files.put(f, false);
                    continue;
                }
            } else {
                if (f.getPermissionLevel() > 0) {
                    files.put(f, false);
                    continue;
                }
            }
            files.put(f, true);
        }

        return files;
    }

    private Map<Folder, Boolean> getFoldersInFolder(Folder folder, @Nullable User user) {
        Map<Folder, Boolean> folders = new HashMap<>();

        // if user != null and folder perm level > user perm level, set accessible to false
        // if user == null and folder perm level > 0, set accessible to false
        // if folder perm level == 0, set accessible to true
        for (Folder f : folderService.getFoldersInFolder(folder.getId())) {
            if (user != null) {
                if (f.getPermissionLevel() > user.getPermissionLevel()) {
                    folders.put(f, false);
                    continue;
                }
            } else {
                if (f.getPermissionLevel() > 0) {
                    folders.put(f, false);
                    continue;
                }
            }
            folders.put(f, true);
        }

        return folders;
    }

    private Folder resolvePath(String path, List<Folder> all) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return all.stream()
                    .filter(f -> f.getParentId() == 0)
                    .findFirst()
                    .orElse(null);
        }

        String[] parts = Arrays.stream(path.split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);

        Folder current = all.stream()
                .filter(f -> f.getParentId() == 0)
                .findFirst()
                .orElse(null);

        for (String part : parts) {
            if (current == null) return null;
            int parentId = current.getId();
            current = all.stream()
                    .filter(f -> f.getName().equals(part) && f.getParentId() == parentId)
                    .findFirst()
                    .orElse(null);
        }

        return current;
    }
}
