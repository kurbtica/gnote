package com.stsau.slam2.API_Gnotes.controller;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MainController {
    @GetMapping("/api")
    public ResponseEntity<Map<String, String>> checkHealth() {
        // Création d'une réponse JSON simple
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "L'API est fonctionnelle !");

        // Retourne un statut 200 OK avec le corps JSON
        return ResponseEntity.ok(response);
    }
}
