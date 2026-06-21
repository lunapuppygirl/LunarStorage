package dev.lunapuppygirl.lunarstorage.services;

import dev.lunapuppygirl.lunarstorage.managers.JsonFileManager;
import dev.lunapuppygirl.lunarstorage.utils.MiscUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PowService {
    private final Map<String, Challenge> pendingChallenges = new ConcurrentHashMap<>();
    private final JwtService jwtService;
    private final JsonFileManager jsonFileManager;

    public PowService(JwtService jwtService, JsonFileManager jsonFileManager) {
        this.jwtService = jwtService;
        this.jsonFileManager = jsonFileManager;
    }

    public Challenge createChallenge() {
        Challenge ch = new Challenge(jsonFileManager.getInt("pow.difficulty", jsonFileManager.getConfigFile(), 18));
        pendingChallenges.put(ch.getId(), ch);
        return ch;
    }

    public boolean verify(String challengeId, long nonce) {
        Challenge ch = pendingChallenges.remove(challengeId);
        if (ch == null || ch.isExpired()) return false;

        String input = ch.getPrefix() + ":" + nonce;
        byte[] digest = sha256(input);
        return hasLeadingZeroBits(digest, ch.getDifficulty());
    }

    private byte[] sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private boolean hasLeadingZeroBits(byte[] hash, int bits) {
        int bytes = bits / 8;
        int remaining = bits % 8;

        for (int i=0; i<bytes; i++) {
            if (hash[i] != 0) return false;
        }

        if (remaining > 0) {
            int mask = 0xFF << (8 - remaining) & 0xFF;
            if ((hash[bytes] & mask) != 0) return false;
        }

        return true;
    }

    public boolean isAfterVerification(HttpServletRequest request) {
        if (!jsonFileManager.getBoolean("pow.enabled", jsonFileManager.getConfigFile(), true)) return true;

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


    @Getter
    public static class Challenge {
        private final String id;
        private final String prefix;
        private final int difficulty;
        private final Instant issuedAt;
        private final Instant expiresAt;

        public Challenge(int difficulty) {
            this.id = UUID.randomUUID().toString();
            this.prefix = UUID.randomUUID().toString().replace("-", "");
            this.difficulty = difficulty;
            this.issuedAt = Instant.now();
            this.expiresAt = issuedAt.plusSeconds(300); // 5 minutes
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
