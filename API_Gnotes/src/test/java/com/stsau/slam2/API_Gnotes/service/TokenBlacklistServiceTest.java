package com.stsau.slam2.API_Gnotes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBlacklistServiceTest {

    private TokenBlacklistService blacklistService;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        blacklistService = new TokenBlacklistService(jwtService);
    }

    @Test
    void testBlacklistAndCheck() {
        String token = "dummy.jwt.token";
        
        assertFalse(blacklistService.isBlacklisted(token));
        
        blacklistService.blacklistToken(token);
        
        assertTrue(blacklistService.isBlacklisted(token));
    }
}
