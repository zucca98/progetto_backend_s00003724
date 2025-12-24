package com.epicode.Progetto_Backend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CorsConfig - Configurazione CORS (Cross-Origin Resource Sharing).
 * 
 * Questa classe configura il comportamento CORS dell'applicazione per permettere
 * richieste HTTP da origini diverse (frontend React, Angular, Vue, etc.).
 * 
 * CORS è necessario quando il frontend e il backend sono hostati su domini/porte diverse.
 * Ad esempio: frontend su localhost:3000 e backend su localhost:8080.
 * 
 * La configurazione viene letta da application.properties con il prefisso "cors":
 * - cors.allowed-origins: Origini consentite (separate da virgola)
 * - cors.allowed-methods: Metodi HTTP consentiti (GET, POST, PUT, DELETE, etc.)
 * - cors.allowed-headers: Headers consentiti (* per tutti)
 * - cors.allow-credentials: Permettere invio di credenziali (cookie, authorization headers)
 * 
 * Valori di default:
 * - Origins: localhost:3000, localhost:5173 (Vite), localhost:4200 (Angular)
 * - Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
 * - Headers: * (tutti)
 * - Credentials: true
 * - Max Age: 3600 secondi (1 ora) per cache delle richieste preflight
 */
@Configuration
public class CorsConfig {
    
    /** Origini consentite per le richieste CORS (default: localhost per sviluppo) */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:4200}")
    private String allowedOrigins;
    
    /** Metodi HTTP consentiti per le richieste CORS */
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;
    
    /** Headers consentiti per le richieste CORS (* per tutti) */
    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;
    
    /** Permettere l'invio di credenziali (cookies, authorization headers) */
    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;
    
    /**
     * Crea e configura il filtro CORS che viene applicato a tutte le richieste.
     * 
     * Il filtro:
     * - Processa le richieste preflight (OPTIONS)
     * - Aggiunge gli header CORS appropriati alle risposte
     * - Permette/nega le richieste in base alla configurazione
     * 
     * @return CorsFilter configurato e pronto per essere registrato nella catena di filtri
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Parse allowed origins from comma-separated string
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        
        // Parse allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        config.setAllowedMethods(methods);
        
        // Parse allowed headers
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            config.setAllowedHeaders(headers);
        }
        
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(3600L); // Cache preflight for 1 hour
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

