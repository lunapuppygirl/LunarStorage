package dev.lunapuppygirl.lunarstorage.web.frontend;

import dev.lunapuppygirl.lunarstorage.managers.JsonFileManager;
import dev.lunapuppygirl.lunarstorage.services.PowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.TimeZone;

@Controller
@RequestMapping("/login")
class LoginController {
    private final PowService powService;
    private final JsonFileManager jsonFileManager;
    private final String clientId;

    LoginController(PowService powService, JsonFileManager jsonFileManager, @Value("${spring.security.oauth2.client.registration.discord.client-id}") String clientId) {
        this.powService = powService;
        this.jsonFileManager = jsonFileManager;
        this.clientId = clientId;
    }

    @GetMapping
    public String login(Model model, HttpServletRequest request) {
        model.addAttribute("year", Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR));
        if (!powService.isAfterVerification(request)) return "verification";


        model.addAttribute("discord_enabled", jsonFileManager.getBoolean("login.discord.enabled", jsonFileManager.getConfigFile(), true));

        return "login";
    }
}
