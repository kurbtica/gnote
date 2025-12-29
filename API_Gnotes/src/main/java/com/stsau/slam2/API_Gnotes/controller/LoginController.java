package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.repository.UserRepository;
import com.stsau.slam2.API_Gnotes.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public LoginController(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/login")
    // On change le retour en Map<String, String> pour renvoyer du JSON propre
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {

        // 1. Authentifier l'utilisateur (Vérifie login/mdp)
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username(), loginRequest.password());

        Authentication authenticationResponse =
                this.authenticationManager.authenticate(authenticationRequest);

        // 2. Si on arrive ici, c'est que le login est bon. On génère le token.
        String token = jwtService.generateToken(authenticationResponse);

        com.stsau.slam2.API_Gnotes.model.User user = userRepository.findByEmail(loginRequest.username())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 4. Construction de la réponse JSON complète
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("id", user.getId());
        responseData.put("nom", user.getNom());
        responseData.put("prenom", user.getPrenom());
        responseData.put("email", user.getEmail());
        responseData.put("adresse", user.getAdresse());
        responseData.put("telephone", user.getTelephone());
        responseData.put("role", user.getRole());

        return ResponseEntity.ok(responseData);
    }

    public record LoginRequest(String username, String password) {
    }
}