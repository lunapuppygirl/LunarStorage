package dev.lunapuppygirl.lunarstorage.oauth2.impl;

import dev.lunapuppygirl.lunarstorage.database.repositories.users.User;
import dev.lunapuppygirl.lunarstorage.database.services.UserService;
import dev.lunapuppygirl.lunarstorage.oauth2.OAuth2ProviderHandler;
import dev.lunapuppygirl.lunarstorage.services.JwtService;
import dev.lunapuppygirl.lunarstorage.utils.MiscUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.UUID;

@Component
public class DiscordOAuth2ProviderHandler implements OAuth2ProviderHandler {
    private final JwtService jwtService;
    private final UserService userService;

    public DiscordOAuth2ProviderHandler(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public String getProviderId() {
        return "discord";
    }

    @Override
    public void handle(OAuth2User oAuth2User, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String discordId = oAuth2User.getAttribute("id");
        String username = oAuth2User.getAttribute("username");

        if (discordId == null || username == null) return;

        String ip = MiscUtils.getIp(request);
        User user = userService.getByDiscordId(Long.parseLong(discordId));

        if (user == null) {
            UUID uuid = UUID.randomUUID();
            userService.createUser(uuid, username, Long.parseLong(discordId), 0, InetAddress.getByName(ip));
            user = userService.getByUuid(uuid);
        }

        String jwt = jwtService.generateUserToken(user.getUuid());

        ResponseCookie cookie = ResponseCookie.from("token", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.sendRedirect("/storage");
    }
}
