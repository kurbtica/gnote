package com.stsau.slam2.API_Gnotes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void testTokenGenerationAndExtraction() {
        // Fake Authentication
        Authentication auth = new Authentication() {
            @Override public Collection getAuthorities() { return null; }
            @Override public Object getCredentials() { return null; }
            @Override public Object getDetails() { return null; }
            @Override public Object getPrincipal() { return null; }
            @Override public boolean isAuthenticated() { return true; }
            @Override public void setAuthenticated(boolean isAuthenticated) {}
            @Override public String getName() { return "test@test.com"; }
        };

        String token = jwtService.generateToken(auth);
        
        assertNotNull(token);
        
        String username = jwtService.extractUsername(token);
        assertEquals("test@test.com", username);
    }
}
