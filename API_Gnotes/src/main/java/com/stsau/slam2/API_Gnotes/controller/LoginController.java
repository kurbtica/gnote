package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.repository.UserRepository;
import com.stsau.slam2.API_Gnotes.service.JwtService;
import org.springframework.http.HttpStatus;
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

	public LoginController(AuthenticationManager authenticationManager, JwtService jwtService,
			UserRepository userRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@PostMapping("/api/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody(required = false) LoginRequest loginRequest,
			Authentication existingAuth) {

		Authentication authenticationResponse;
		String username;

		// CAS 1 : L'utilisateur a utilisé "curl -u user:pass" (Basic Auth)
		// Spring Security a déjà fait l'authentification avant d'arriver ici
		if (existingAuth != null && existingAuth.isAuthenticated()) {
			authenticationResponse = existingAuth;
			username = existingAuth.getName();
		}
		// CAS 2 : L'utilisateur a envoyé un JSON (ton code d'origine)
		else if (loginRequest != null && loginRequest.username() != null) {
			Authentication authenticationRequest = UsernamePasswordAuthenticationToken
					.unauthenticated(loginRequest.username(), loginRequest.password());

			authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
			username = loginRequest.username();
		}
		// CAS 3 : Rien n'a été fourni
		else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		// 2. Génération du token
		String token = jwtService.generateToken(authenticationResponse);

		com.stsau.slam2.API_Gnotes.model.User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

		// 4. Construction de la réponse JSON
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("token", token);
		responseData.put("id", user.getId());
		responseData.put("nom", user.getNom());
		responseData.put("prenom", user.getPrenom());
		responseData.put("email", user.getEmail());
		responseData.put("role", user.getRole());

		return ResponseEntity.ok(responseData);
	}

	public record LoginRequest(String username, String password) {
	}
}