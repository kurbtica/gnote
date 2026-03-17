package com.stsau.slam2.API_Gnotes.config;

import com.stsau.slam2.API_Gnotes.service.JwtService;
import com.stsau.slam2.API_Gnotes.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final TokenBlacklistService blacklistService;
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService,
			TokenBlacklistService blacklistService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.blacklistService = blacklistService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		System.out.println(" Filtre JWT appelé pour : " + path);

		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			System.out.println("❌ Pas de header Authorization ou pas de Bearer");
			filterChain.doFilter(request, response);
			return;
		}
		System.out.println("✅ Token trouvé ! Tentative d'extraction...");

		final String jwt = authHeader.substring(7);
		final String userEmail;

		// Blocage blacklist (correctement placé avant le parsing)
		if (blacklistService.isBlacklisted(jwt)) {
			System.out.println("⛔ Tentative d'accès avec un token blacklisté !");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		try {
			// ⚠️ C'est ici que ça plantait : on enveloppe l'extraction dans un try
			userEmail = jwtService.extractUsername(jwt);

			if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

				if (jwtService.isTokenValid(jwt, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}

			// Si tout s'est bien passé, on continue la chaîne
			filterChain.doFilter(request, response);

		} catch (ExpiredJwtException e) {
			// ✅ GESTION DU TOKEN EXPIRÉ
			System.out.println("⏰ Le token a expiré : " + e.getMessage());

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Code 401
			response.setContentType("application/json");
			response.getWriter().write("{\"error\": \"Token expiré\", \"message\": \"Veuillez vous reconnecter\"}");

			// IMPORTANT : On ne fait PAS filterChain.doFilter() ici, on arrête la requête.

		} catch (Exception e) {
			// Gestion des autres erreurs (signature invalide, token malformé...)
			System.out.println("⚠️ Erreur lors de l'analyse du token : " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Ou 403/401 selon préférence
			response.getWriter().write("{\"error\": \"Token invalide\"}");
		}
	}
}