package dev.lunapuppygirl.lunarstorage.web.frontend;

import dev.lunapuppygirl.lunarstorage.services.AnnouncementService;
import dev.lunapuppygirl.lunarstorage.services.PowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Calendar;
import java.util.TimeZone;

@Controller
@RequestMapping("/")
public class HomeController {
    private final PowService powService;
    private final AnnouncementService announcementService;

    public HomeController(PowService powService, AnnouncementService announcementService) {
        this.powService = powService;
        this.announcementService = announcementService;
    }

    @GetMapping
    public String home(HttpServletRequest request, Model model) {
        model.addAttribute("year", Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR));

        if (!powService.isAfterVerification(request)) {
            return "verification";
        }

        announcementService.setCurrentAnnouncement(AnnouncementService.Level.HIGH, "Example Announcement", "Lorem ipsum dolor sit amet consectetur adipiscing elit. Consectetur adipiscing elit quisque faucibus ex sapien vitae. Ex sapien vitae pellentesque sem placerat in id. Placerat in id cursus mi pretium tellus duis. Pretium tellus duis convallis tempus leo eu aenean.", true);

        model.addAttribute("announcement", announcementService.getCurrentAnnouncement());

        return "home";
    }
}
