package com.stsau.slam2.API_Gnotes.controller;
import com.stsau.slam2.API_Gnotes.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

    private final TokenBlacklistService blacklistService;

    public LogoutController(TokenBlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            blacklistService.blacklistToken(token);
            return ResponseEntity.ok("Token invalidé avec succès.");
        }

        return ResponseEntity.badRequest().body("Pas de token fourni");
    }
}