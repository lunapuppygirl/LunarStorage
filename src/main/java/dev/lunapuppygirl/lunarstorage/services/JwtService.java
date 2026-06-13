package dev.lunapuppygirl.lunarstorage.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final String jwtSecret;

    public JwtService(@Value("${app.secrets.tokens}") String secret) {
        this.jwtSecret = secret;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return claims.getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public String generateUserToken(UUID uuid) {
        return Jwts.builder()
                .id(uuid.toString())
                .issuer("oauth")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + 7 * 24 * 60 * 60 * 1000)) // 7 days
                .signWith(this.getSigningKey())
                .compact();
    }

    public String generateVerificationToken(String ip) {
        return Jwts.builder()
                .id(ip)
                .issuer("verification")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + 15 * 60 * 1000)) // 15 minutes
                .signWith(this.getSigningKey())
                .compact();
    }
}
