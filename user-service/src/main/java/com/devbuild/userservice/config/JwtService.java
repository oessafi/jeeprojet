package com.devbuild.userservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority; // <-- Importer
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap; // <-- Importer
import java.util.Map; // <-- Importer
import java.util.function.Function;
import java.util.stream.Collectors; // <-- Importer

@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    // Extrait l'email (subject) du token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Génère un token (méthode publique principale)
    public String generateToken(UserDetails userDetails) {
        // Créer un Map pour les claims personnalisés
        Map<String, Object> extraClaims = new HashMap<>();

        // Extraire le rôle des autorités
        // L'implémentation UserDetails (dans User.java) fournit "ROLE_NOM_DU_ROLE"
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("") // Au cas où il n'y a pas d'autorité
                .replace("ROLE_", ""); // Retirer le préfixe "ROLE_"

        extraClaims.put("role", role);

        return generateToken(extraClaims, userDetails);
    }

    // Génère un token avec des claims supplémentaires
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts.builder()
                .claims(extraClaims) // <-- Ajout des claims personnalisés
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 heures
                .signWith(getSignInKey())
                .compact();
    }

    // Valide le token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}