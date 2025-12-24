package com.epicode.Progetto_Backend.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserUpdateDTO - Data Transfer Object per l'aggiornamento di un utente.
 * 
 * Utilizzato negli endpoint:
 * - PUT /api/users/{id} (solo ADMIN)
 * - PUT /api/users/me (utente corrente)
 * 
 * Tutti i campi sono opzionali - vengono aggiornati solo i campi non null.
 * Il campo profileImage deve contenere un URL valido (solitamente da Cloudinary).
 * 
 * Note:
 * - L'email non viene generalmente aggiornata tramite questo DTO
 * - Il campo profileImage deve essere un URL completo (es: https://res.cloudinary.com/...)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {
    
    /** Email dell'utente (opzionale, se fornita deve essere valida) */
    @Email(message = "Email non valida")
    private String email;
    
    /** Nome dell'utente (opzionale) */
    private String nome;
    
    /** Cognome dell'utente (opzionale) */
    private String cognome;
    
    /** URL dell'immagine profilo (opzionale, solitamente da Cloudinary) */
    private String profileImage;
}
