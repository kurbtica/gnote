package com.stsau.slam2.API_Gnotes.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final JwtService jwtService; // On a besoin de lui pour lire la date

    // Injection du JwtService via constructeur
    public TokenBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    // Tâche automatique : S'exécute toutes les heures nettoie les token perimée
    @Scheduled(fixedRate = 3600000)
    public void cleanUpExpiredTokens() {
        System.out.println("🧹 Nettoyage de la liste noire... Taille actuelle : " + blacklistedTokens.size());

        Iterator<String> iterator = blacklistedTokens.iterator();
        Date now = new Date();

        while (iterator.hasNext()) {
            String token = iterator.next();
            try {

                Date expiration = jwtService.extractExpiration(token);
                if (expiration.before(now)) {
                    iterator.remove();
                }
            } catch (Exception e) {

                iterator.remove();
            }
        }
    }
}