package com.epicode.Progetto_Backend.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter - Filtro Spring Security per l'autenticazione JWT.
 * 
 * Intercetta tutte le richieste HTTP e verifica la presenza di un token JWT
 * nell'header "Authorization" con formato "Bearer {token}".
 * 
 * Funzionamento:
 * 1. Estrae il token JWT dall'header Authorization
 * 2. Valida il token e estrae lo username
 * 3. Carica i dettagli dell'utente tramite UserDetailsService
 * 4. Crea un'Authentication e la imposta nel SecurityContext
 * 
 * Se il token è valido, l'utente viene autenticato automaticamente per la richiesta corrente.
 * Se il token è assente o non valido, la richiesta procede senza autenticazione
 * e Spring Security gestirà l'accesso in base alle regole di autorizzazione.
 * 
 * Il filtro viene eseguito prima di UsernamePasswordAuthenticationFilter
 * per permettere l'autenticazione JWT su tutte le richieste.
 * 
 * @see com.epicode.Progetto_Backend.security.JwtTokenProvider
 * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * Metodo principale del filtro che viene eseguito per ogni richiesta HTTP.
     * 
     * Estrae il token JWT dalla richiesta, lo valida e imposta l'autenticazione
     * nel SecurityContext se il token è valido.
     * 
     * Se il token è assente o non valido, la richiesta procede senza autenticazione
     * e Spring Security gestirà l'accesso in base alle regole di autorizzazione.
     * 
     * @param request Richiesta HTTP
     * @param response Risposta HTTP
     * @param filterChain Catena di filtri da eseguire
     * @throws ServletException se si verifica un errore del servlet
     * @throws IOException se si verifica un errore di I/O
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            // Debug: verifica se il token viene ricevuto
            if (request.getRequestURI().contains("/manutenzioni/locatario")) {
                System.out.println("JWT Filter - URI: " + request.getRequestURI());
                System.out.println("JWT Filter - Token presente: " + StringUtils.hasText(jwt));
            }
            
            if (StringUtils.hasText(jwt)) {
                try {
                    // Estrae lo username dal token
                    String username = tokenProvider.extractUsername(jwt);
                    // Carica i dettagli dell'utente dal database
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // Valida il token e imposta l'autenticazione
                    if (tokenProvider.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        // Debug: verifica autenticazione e ruoli
                        if (request.getRequestURI().contains("/manutenzioni/locatario")) {
                            System.out.println("JWT Filter - User: " + userDetails.getUsername());
                            System.out.println("JWT Filter - Authorities: " + userDetails.getAuthorities());
                        }
                    }
                } catch (IllegalArgumentException | io.jsonwebtoken.JwtException | org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                    logger.error("Could not set user authentication in security context for URI: " + request.getRequestURI(), ex);
                    // Non bloccare la richiesta, lascia che Spring Security gestisca l'autenticazione
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception ex) {
            logger.error("Unexpected error in JWT filter for URI: " + request.getRequestURI(), ex);
            SecurityContextHolder.clearContext();
        }
        
        // Continua con la catena di filtri
        filterChain.doFilter(request, response);
    }
    
    /**
     * Estrae il token JWT dall'header Authorization della richiesta.
     * 
     * Il token deve essere nel formato: "Bearer {token}"
     * 
     * @param request Richiesta HTTP
     * @return Token JWT se presente e valido, null altrimenti
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
