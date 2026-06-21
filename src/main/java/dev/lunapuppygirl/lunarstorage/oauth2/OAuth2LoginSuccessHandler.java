package dev.lunapuppygirl.lunarstorage.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final Map<String, OAuth2ProviderHandler> handlers;

    public OAuth2LoginSuccessHandler(List<OAuth2ProviderHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(OAuth2ProviderHandler::getProviderId, Function.identity()));
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) return;

        String providerId = token.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = token.getPrincipal();

        OAuth2ProviderHandler handler = handlers.get(providerId);

        if (handler == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported OAuth2 provider.");
            return;
        }

        handler.handle(oAuth2User, request, response);
    }
}
