package com.epicode.Progetto_Backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig - Configurazione globale per la gestione delle richieste HTTP.
 * 
 * Questa classe configura gli interceptor che vengono eseguiti per ogni richiesta HTTP
 * ricevuta dall'applicazione, prima che arrivi ai controller.
 * 
 * Interceptor registrati:
 * - RateLimitingInterceptor: Limita il numero di richieste per IP per prevenire abusi
 * 
 * Pattern applicati:
 * - Intercetta tutte le richieste che iniziano con "/api/**"
 * - Esclude gli endpoint di autenticazione ("/api/auth/**") dal rate limiting
 * - Esclude gli endpoint Swagger/OpenAPI ("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**")
 * 
 * Questa classe può essere estesa per aggiungere altri interceptor come:
 * - Logging interceptor (registrazione di tutte le richieste)
 * - Request/Response transformation interceptor
 * - Performance monitoring interceptor
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /** Interceptor per limitare il numero di richieste per IP */
    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;
    
    /**
     * Registra gli interceptor nella catena di elaborazione delle richieste.
     * 
     * L'interceptor viene eseguito:
     * - Prima che la richiesta arrivi al controller
     * - Può permettere o bloccare la richiesta
     * - Può modificare la richiesta/risposta
     * 
     * @param registry Registry per registrare gli interceptor
     */
    @Override
    @SuppressWarnings("null")
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**");
    }
}

