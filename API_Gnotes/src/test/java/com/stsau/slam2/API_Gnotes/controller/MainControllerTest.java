package com.stsau.slam2.API_Gnotes.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MainControllerTest {

    private MainController mainController;

    @BeforeEach
    void setUp() {
        mainController = new MainController();
    }

    @Test
    void testCheckHealth() {
        ResponseEntity<Map<String, String>> response = mainController.checkHealth();
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("OK", response.getBody().get("status"));
        assertEquals("L'API est fonctionnelle !", response.getBody().get("message"));
    }
}
