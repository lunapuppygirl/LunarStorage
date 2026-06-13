package dev.lunapuppygirl.lunarstorage.web.backend.api.v1.pow;

import dev.lunapuppygirl.lunarstorage.services.JwtService;
import dev.lunapuppygirl.lunarstorage.services.PowService;
import dev.lunapuppygirl.lunarstorage.utils.MiscUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Cookie;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pow")
class PowController {
    private final PowService powService;
    private final JwtService jwtService;

    public PowController(PowService powService, JwtService jwtService) {
        this.powService = powService;
        this.jwtService = jwtService;
    }

    @GetMapping("/challenge")
    public ResponseEntity<Map<String, Object>> challenge() {
        Map<String, Object> resp = new HashMap<>();
        PowService.Challenge ch = powService.createChallenge();

        resp.put("id", ch.getId());
        resp.put("prefix", ch.getPrefix());
        resp.put("difficulty", ch.getDifficulty());
        resp.put("expiresAt", ch.getExpiresAt());

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> body) {
        if (powService.isAfterVerification(request)) return ResponseEntity.ok(Map.of("success", true));

        if (!body.containsKey("id") && !body.containsKey("nonce")) {
            return ResponseEntity.badRequest().build();
        }

        String ip = MiscUtils.getIp(request);
        String challengeId = body.get("id").toString();
        long nonce = Long.parseLong(body.get("nonce").toString());

        boolean ok = powService.verify(challengeId, nonce);
        if (ok) {
            String jwt = jwtService.generateVerificationToken(ip);

            ResponseCookie cookie = ResponseCookie.from("_pow", jwt)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(-1)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok(Map.of("success", true));
        }

        return ResponseEntity.status(403).body(Map.of("success", false));
    }
}
