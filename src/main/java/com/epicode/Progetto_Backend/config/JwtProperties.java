package com.epicode.Progetto_Backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * JwtProperties - Classe di configurazione per le proprietà JWT.
 * 
 * Questa classe mappa le proprietà di configurazione relative alla generazione
 * e validazione dei token JWT utilizzati per l'autenticazione.
 * 
 * Le proprietà vengono lette dal file env.properties con il prefisso "jwt":
 * - jwt.secret: Chiave segreta utilizzata per firmare e verificare i token JWT
 * - jwt.expiration: Durata di validità del token in millisecondi (es: 86400000 = 24 ore)
 * 
 * Utilizzata da JwtTokenProvider per generare e validare i token.
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /** Chiave segreta per firmare e verificare i token JWT (deve essere sicura e non condivisa) */
    private String secret;
    
    /** Durata di validità del token in millisecondi (es: 86400000 = 24 ore) */
    private long expiration;
}

