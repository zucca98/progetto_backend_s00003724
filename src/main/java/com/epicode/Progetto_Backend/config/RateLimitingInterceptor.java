package com.epicode.Progetto_Backend.config;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * RateLimitingInterceptor - Interceptor per limitare il numero di richieste per IP.
 * 
 * Questo interceptor implementa il rate limiting utilizzando l'algoritmo Token Bucket
 * della libreria Bucket4j per prevenire abusi e attacchi DDoS limitando il numero
 * di richieste che un singolo IP può effettuare in un determinato periodo di tempo.
 * 
 * Configurazione attuale:
 * - Limite: 100 richieste per minuto per IP
 * - Implementazione: Token Bucket Algorithm (rifill intervallato)
 * - Scope: Applicato a tutte le richieste "/api/**" (esclusi gli endpoint di auth)
 * 
 * Funzionamento:
 * 1. Identifica l'IP del client (considera anche X-Forwarded-For per proxy/load balancer)
 * 2. Crea o recupera un "bucket" di token per quell'IP
 * 3. Consuma un token per ogni richiesta
 * 4. Se il bucket è vuoto (limite superato), restituisce 429 Too Many Requests
 * 5. I token vengono ricaricati automaticamente ogni minuto
 * 
 * Risposta quando il limite viene superato:
 * - Status Code: 429 Too Many Requests
 * - Body: {"error":"Rate limit exceeded. Please try again later."}
 * 
 * L'interceptor è registrato in WebConfig e viene eseguito prima dei controller.
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingInterceptor.class);
    
    /** Limite di richieste consentite per minuto per IP */
    private static final int REQUESTS_PER_MINUTE = 100;
    
    /** Mappa thread-safe che memorizza i bucket di token per ogni IP */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    /**
     * Metodo chiamato prima che la richiesta venga processata dal controller.
     * 
     * Controlla se l'IP ha ancora token disponibili nel bucket:
     * - Se sì: consuma un token e permette la richiesta (return true)
     * - Se no: blocca la richiesta e restituisce 429 Too Many Requests (return false)
     * 
     * @param request Richiesta HTTP in arrivo
     * @param response Risposta HTTP da inviare
     * @param handler Handler che gestirà la richiesta (controller method)
     * @return true se la richiesta può procedere, false se viene bloccata
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // Skip rate limiting for auth endpoints (login/register devono sempre essere disponibili)
        if (request.getRequestURI().startsWith("/api/auth")) {
            return true;
        }
        
        // Ottieni l'IP del client e crea/recupera il bucket per quell'IP
        String clientIp = getClientIpAddress(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, ip -> createBucket());
        
        // Prova a consumare un token dal bucket
        if (bucket.tryConsume(1)) {
            // Token disponibile: permetti la richiesta
            return true;
        } else {
            // Nessun token disponibile: limite superato
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            try {
                response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            } catch (IOException e) {
                logger.error("Error writing rate limit response", e);
            }
            return false;
        }
    }
    
    /**
     * Crea un nuovo bucket di token con il limite configurato.
     * 
     * Il bucket utilizza il Token Bucket Algorithm:
     * - Capacità iniziale: REQUESTS_PER_MINUTE token
     * - Rifill: REQUESTS_PER_MINUTE token ogni minuto (refill intervallato)
     * 
     * Esempio: Con 100 richieste/minuto
     * - All'inizio: 100 token disponibili
     * - Dopo 50 richieste: 50 token rimanenti
     * - Dopo 1 minuto senza richieste: ricarica a 100 token
     * 
     * @return Nuovo bucket configurato
     */
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(REQUESTS_PER_MINUTE)
                .refillIntervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
    
    /**
     * Estrae l'indirizzo IP del client dalla richiesta HTTP.
     * 
     * Considera anche gli header impostati da proxy/load balancer:
     * 1. X-Forwarded-For: Header standard per IP originale quando dietro un proxy
     * 2. X-Real-IP: Header alternativo per IP originale
     * 3. request.getRemoteAddr(): IP diretto del client (fallback)
     * 
     * @param request Richiesta HTTP
     * @return Indirizzo IP del client come stringa
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Prova prima X-Forwarded-For (comune quando dietro un proxy/load balancer)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For può contenere multipli IP separati da virgola
            // Il primo è sempre l'IP originale del client
            return xForwardedFor.split(",")[0].trim();
        }
        // Fallback su X-Real-IP
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        // Ultimo fallback: IP diretto
        return request.getRemoteAddr();
    }
}

