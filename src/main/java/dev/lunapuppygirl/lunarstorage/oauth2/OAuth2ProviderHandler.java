package dev.lunapuppygirl.lunarstorage.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;

public interface OAuth2ProviderHandler {
    String getProviderId();
    void handle(OAuth2User oAuth2User, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
