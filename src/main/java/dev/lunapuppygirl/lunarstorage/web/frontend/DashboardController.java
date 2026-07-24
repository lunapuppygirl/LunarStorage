package dev.lunapuppygirl.lunarstorage.web.frontend;

import dev.lunapuppygirl.lunarstorage.database.repositories.users.User;
import dev.lunapuppygirl.lunarstorage.database.services.BlacklistService;
import dev.lunapuppygirl.lunarstorage.database.services.FileService;
import dev.lunapuppygirl.lunarstorage.database.services.FolderService;
import dev.lunapuppygirl.lunarstorage.database.services.UserService;
import dev.lunapuppygirl.lunarstorage.managers.JsonFileManager;
import dev.lunapuppygirl.lunarstorage.services.AnnouncementService;
import dev.lunapuppygirl.lunarstorage.services.PowService;
import dev.lunapuppygirl.lunarstorage.utils.MiscUtils;
import dev.lunapuppygirl.lunarstorage.utils.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final UserService userService;
    private final JsonFileManager jsonFileManager;
    private final FileService fileService;
    private final FolderService folderService;
    private final BlacklistService blacklistService;
    private final PowService powService;
    private final AnnouncementService announcementService;

    public DashboardController(UserService userService, JsonFileManager jsonFileManager, FileService fileService,
                               FolderService folderService, BlacklistService blacklistService,
                               PowService powService, AnnouncementService announcementService) {
        this.userService = userService;
        this.jsonFileManager = jsonFileManager;
        this.fileService = fileService;
        this.folderService = folderService;
        this.blacklistService = blacklistService;
        this.powService = powService;
        this.announcementService = announcementService;
    }

    @GetMapping(path = {"", "/", "/main"})
    @PreAuthorize("@permissions.hasPermissionLevel(@securityConfig.minDashboardLevel)")
    public String mainDashboard(HttpServletRequest req, HttpServletResponse resp, Model model) {
        model.addAttribute("year", Year.now().getValue());

        if (!powService.isAfterVerification(req)) return "verification";

        User user = userService.getFromRequest(req);
        if (user == null) return "redirect:/login";

        Map<Integer, Integer> lastWeek = jsonFileManager.getMap(
                "requests.last_week", jsonFileManager.getStatsFile(), Integer.class, Integer.class);
        Map<Integer, Integer> lastDay = jsonFileManager.getMap(
                "requests.last_day", jsonFileManager.getStatsFile(), Integer.class, Integer.class);
        Map<Integer, Integer> lastHour = jsonFileManager.getMap(
                "requests.last_hour", jsonFileManager.getStatsFile(), Integer.class, Integer.class);

        model.addAttribute("requestsLastWeek", toOrderedValues(lastWeek, 1, 7));   // days 1-7
        model.addAttribute("requestsLastDay", toOrderedValues(lastDay, 0, 23));    // hours 0-23
        model.addAttribute("requestsLastHour", toOrderedValues(lastHour, 0, 59));  // minutes 0-59
        model.addAttribute("lastUpdated", MiscUtils.formatDate(jsonFileManager.getLong("updated", jsonFileManager.getStatsFile(), 0)));
        model.addAttribute("user", user);

        return "dashboard/main";
    }

    @GetMapping("/users")
    @PreAuthorize("@permissions.hasPermissionLevel(@securityConfig.minDashboardLevel) && @permissions.hasPermissionLevel(@securityConfig.minManageUsersLevel)")
    public String users(HttpServletRequest req, HttpServletResponse resp, Model model) {
        model.addAttribute("year", Year.now().getValue());

        if (!powService.isAfterVerification(req)) return "verification";

        User user = userService.getFromRequest(req);
        if (user == null) return "redirect:/login";

        Pagination.PaginatedData<User> users = Pagination.createPaginatedList(userService.getAll(), 20);

        model.addAttribute("user", user);
        model.addAttribute("pages", users);

        return "dashboard/users";
    }


    private List<Integer> toOrderedValues(Map<Integer, Integer> source, int fromInclusive, int toInclusive) {
        List<Integer> values = new ArrayList<>();
        for (int i = fromInclusive; i <= toInclusive; i++) {
            values.add(source.getOrDefault(i, 0));
        }
        return values;
    }
}