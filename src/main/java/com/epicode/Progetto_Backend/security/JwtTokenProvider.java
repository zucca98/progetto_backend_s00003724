package com.epicode.Progetto_Backend.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.epicode.Progetto_Backend.config.JwtProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JwtTokenProvider - Componente per la generazione e validazione di token JWT.
 * 
 * Gestisce l'intero ciclo di vita dei token JWT:
 * - Generazione di token con username, data emissione e scadenza
 * - Estrazione dello username da un token
 * - Validazione di token (verifica scadenza e corrispondenza con UserDetails)
 * 
 * Utilizza la libreria JJWT (io.jsonwebtoken) per la gestione dei token.
 * Il secret key viene recuperato da JwtProperties e utilizzato per firmare
 * e verificare i token con algoritmo HMAC-SHA.
 * 
 * La durata del token è configurabile tramite JwtProperties.expiration
 * (default: 24 ore = 86400000 millisecondi).
 * 
 * @see com.epicode.Progetto_Backend.config.JwtProperties
 * @see com.epicode.Progetto_Backend.security.JwtAuthenticationFilter
 */
@Component
public class JwtTokenProvider {

    @Autowired
    private JwtProperties jwtProperties;
    
    /**
     * Genera la chiave di firma per i token JWT.
     * 
     * Utilizza l'algoritmo HMAC-SHA con il secret recuperato da JwtProperties.
     * 
     * @return SecretKey per firmare e verificare i token
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * Genera un nuovo token JWT per un utente.
     * 
     * Il token contiene:
     * - subject: username (email) dell'utente
     * - issuedAt: data di emissione (ora corrente)
     * - expiration: data di scadenza (ora corrente + expiration da JwtProperties)
     * 
     * Il token viene firmato con la chiave segreta per garantirne l'integrità.
     * 
     * @param username Email dell'utente (utilizzata come subject del token)
     * @return Token JWT firmato come stringa
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Estrae lo username (email) da un token JWT.
     * 
     * Verifica la firma del token prima di estrarre il subject.
     * 
     * @param token Token JWT da cui estrarre lo username
     * @return Email dell'utente (subject del token)
     * @throws io.jsonwebtoken.JwtException se il token non è valido o è scaduto
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    
    /**
     * Valida un token JWT confrontandolo con i dati dell'utente.
     * 
     * Verifica:
     * 1. Che lo username nel token corrisponda a quello dell'utente
     * 2. Che il token non sia scaduto
     * 
     * @param token Token JWT da validare
     * @param userDetails Dettagli dell'utente da confrontare
     * @return true se il token è valido, false altrimenti
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    
    /**
     * Verifica se un token è scaduto.
     * 
     * @param token Token JWT da verificare
     * @return true se il token è scaduto, false altrimenti
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Estrae la data di scadenza da un token JWT.
     * 
     * @param token Token JWT da cui estrarre la scadenza
     * @return Data di scadenza del token
     */
    private Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
}
