package dev.lunapuppygirl.lunarstorage.web.frontend;

import dev.lunapuppygirl.lunarstorage.services.PoWService;
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
    private final PoWService powService;

    public HomeController(PoWService powService) {
        this.powService = powService;
    }

    @GetMapping
    public String home(HttpServletRequest request, Model model) {
        if (!powService.isAfterVerification(request)) {
            return "verification";
        }

        model.addAttribute("year", Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR));

        return "home";
    }
}
