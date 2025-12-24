package com.epicode.Progetto_Backend.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponseDTO - Data Transfer Object per la risposta di autenticazione.
 * 
 * Utilizzato per restituire i dati dell'utente e il token JWT dopo login o registrazione.
 * 
 * Questo DTO viene utilizzato negli endpoint:
 * - POST /api/auth/login
 * - POST /api/auth/register
 * 
 * Il token JWT deve essere incluso nelle successive richieste nell'header:
 * Authorization: Bearer {token}
 * 
 * Il campo "type" è generalmente "Bearer" per indicare il tipo di autenticazione.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    /** Token JWT generato per l'autenticazione (valido 24 ore) */
    private String token;
    
    /** Tipo di token (solitamente "Bearer") */
    private String type;
    
    /** ID dell'utente nel database */
    private Long userId;
    
    /** Email dell'utente */
    private String email;
    
    /** Nome dell'utente */
    private String nome;
    
    /** Cognome dell'utente */
    private String cognome;
    
    /** Set di ruoli dell'utente (es: ["ROLE_ADMIN", "ROLE_MANAGER"]) */
    private Set<String> roles;
}
