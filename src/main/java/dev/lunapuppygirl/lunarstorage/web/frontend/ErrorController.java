package dev.lunapuppygirl.lunarstorage.web.frontend;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Calendar;
import java.util.TimeZone;

@Controller
class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {
    @RequestMapping("/error")
    public String error(Model model, HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        model.addAttribute("code", (int) status);
        model.addAttribute("year", Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR));

        return "error";
    }
}
