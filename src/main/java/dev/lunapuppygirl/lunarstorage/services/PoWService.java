package dev.lunapuppygirl.lunarstorage.services;

import dev.lunapuppygirl.lunarstorage.utils.MiscUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

@Service
public class PoWService {
    private JwtService jwtService;

    public PoWService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public boolean isAfterVerification(HttpServletRequest request) {
        if (request.getCookies() == null) return false;

        String ip = MiscUtils.getIp(request);

        String token = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("_pow"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (token != null && !token.isEmpty()) {
            Claims claims = jwtService.validateToken(token);
            if (claims == null) return false;
            Date now = new Date();
            Date cutoff = new Date(now.getTime() - 15 * 60 * 1000);

            if (
                    claims.getExpiration() != null &&
                            claims.getIssuedAt() != null &&
                            claims.getExpiration().after(now) &&
                            ip.equals(claims.getId()) &&
                            "verification".equals(claims.getIssuer()) &&
                            claims.getIssuedAt().after(cutoff)
            ) {
                return true;
            }
        }

        return false;
    }
}
