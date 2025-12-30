package com.stsau.slam2.API_Gnotes.config;

import com.stsau.slam2.API_Gnotes.service.JwtService;
import com.stsau.slam2.API_Gnotes.service.TokenBlacklistService;
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

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, TokenBlacklistService blacklistService) {
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

        // 1. Récupérer le header "Authorization"

        final String jwt;
        final String userEmail;
        jwt = authHeader.substring(7);

        if (blacklistService.isBlacklisted(jwt)) {
            System.out.println("⛔ Tentative d'accès avec un token blacklisté !");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            return; // On arrête tout ici
        }

        userEmail = jwtService.extractUsername(jwt);

        // 4. Si on a un email et que l'utilisateur n'est pas encore connecté
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Si le token est valide, on connecte l'utilisateur manuellement
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // C'est ICI que Spring Security note que l'utilisateur est connecté !
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. On passe à la suite
        filterChain.doFilter(request, response);
    }
}