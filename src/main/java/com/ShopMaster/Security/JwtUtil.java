package com.ShopMaster.Security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    // Clave aleatoria por defecto (solo sirve hasta que el servidor se reinicia).
    // En producción define la variable de entorno JWT_SECRET (mínimo 32 caracteres)
    // para que los tokens sigan siendo válidos después de cada despliegue.
    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expiration = 1000 * 60 * 60; // 1 hora

    @Value("${jwt.secret:}")
    public void setSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            return;
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /** Vigencia del token en segundos (se informa a la app móvil en el login). */
    public long getExpirationSeconds() {
        return expiration / 1000;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (username.equals(extractedUsername) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expirationDate = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getExpiration();
        return expirationDate.before(new Date());
    }
}