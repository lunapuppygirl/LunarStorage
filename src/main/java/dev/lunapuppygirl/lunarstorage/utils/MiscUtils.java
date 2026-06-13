package dev.lunapuppygirl.lunarstorage.utils;

import jakarta.servlet.http.HttpServletRequest;

public class MiscUtils {
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
