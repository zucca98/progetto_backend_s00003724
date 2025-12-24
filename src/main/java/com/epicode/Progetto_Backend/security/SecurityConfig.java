package com.epicode.Progetto_Backend.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SecurityConfig - Configurazione principale di Spring Security.
 * 
 * Configura l'intero sistema di sicurezza dell'applicazione:
 * - Autenticazione JWT stateless (no sessioni)
 * - Autorizzazione basata su ruoli (@PreAuthorize)
 * - Endpoint pubblici e protetti
 * - Gestione eccezioni di autenticazione e accesso negato
 * 
 * Configurazioni principali:
 * - CSRF disabilitato (non necessario per API stateless con JWT)
 * - SessionCreationPolicy.STATELESS (no sessioni, solo JWT)
 * - Endpoint pubblici: /api/auth/**, /swagger-ui/**, /v3/api-docs/**, /graphiql
 * - Tutti gli altri endpoint richiedono autenticazione
 * - JwtAuthenticationFilter eseguito prima di UsernamePasswordAuthenticationFilter
 * 
 * Gestione errori:
 * - AuthenticationEntryPoint: Gestisce errori 401 (autenticazione mancante)
 * - AccessDeniedHandler: Gestisce errori 403 (accesso negato)
 * 
 * @see com.epicode.Progetto_Backend.security.JwtAuthenticationFilter
 * @see com.epicode.Progetto_Backend.service.CustomUserDetailsService
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Bean per l'encoder delle password (BCrypt).
     * 
     * Utilizzato per hashare le password prima del salvataggio nel database
     * e per verificare le password durante il login.
     * 
     * @return BCryptPasswordEncoder per l'hashing delle password
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean per il provider di autenticazione.
     * 
     * Configura DaoAuthenticationProvider con UserDetailsService e PasswordEncoder
     * per l'autenticazione basata su database.
     * 
     * @return DaoAuthenticationProvider configurato
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Bean per l'AuthenticationManager.
     * 
     * Utilizzato da AuthService per autenticare le credenziali durante il login.
     * 
     * @param config Configurazione di autenticazione
     * @return AuthenticationManager
     * @throws Exception se la configurazione fallisce
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean per la catena di filtri di sicurezza.
     * 
     * Configura:
     * - CSRF disabilitato (API stateless)
     * - Sessioni stateless (solo JWT)
     * - Endpoint pubblici e protetti
     * - Gestione eccezioni di autenticazione e accesso negato
     * - JwtAuthenticationFilter prima di UsernamePasswordAuthenticationFilter
     * 
     * @param http HttpSecurity da configurare
     * @return SecurityFilterChain configurata
     * @throws Exception se la configurazione fallisce
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/graphiql").permitAll() // GraphiQL UI only
                        .requestMatchers("/graphql").authenticated() // GraphQL endpoint requires auth
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                            writeErrorResponse(request, response, HttpStatus.UNAUTHORIZED,
                                "Autenticazione richiesta. Effettua il login per accedere."))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                            writeErrorResponse(request, response, HttpStatus.FORBIDDEN,
                                "Non hai i permessi per accedere a questa risorsa."))
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Scrive una risposta di errore standardizzata in formato JSON.
     * 
     * Utilizzato da AuthenticationEntryPoint e AccessDeniedHandler
     * per restituire errori consistenti in formato JSON invece di HTML.
     * 
     * @param request Richiesta HTTP che ha causato l'errore
     * @param response Risposta HTTP da popolare
     * @param status Codice HTTP status (401 o 403)
     * @param message Messaggio descrittivo dell'errore
     * @throws IOException se si verifica un errore di I/O durante la scrittura
     */
    private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                     HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        var errorBody = new java.util.LinkedHashMap<String, Object>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("message", message);
        errorBody.put("path", request.getRequestURI());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.writeValue(response.getOutputStream(), errorBody);
    }
}
