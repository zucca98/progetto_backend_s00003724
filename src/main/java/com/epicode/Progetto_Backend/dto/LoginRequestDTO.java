package com.epicode.Progetto_Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRequestDTO - Data Transfer Object per la richiesta di login.
 * 
 * Utilizzato nell'endpoint POST /api/auth/login per autenticare un utente esistente.
 * 
 * Le credenziali vengono validate e confrontate con quelle nel database.
 * La password viene verificata tramite BCrypt matching.
 * 
 * Validazioni:
 * - email: Deve essere obbligatoria e in formato email valido
 * - password: Deve essere obbligatoria (in plaintext, verrà hashata dal servizio)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    
    /** Email dell'utente (univoca nel sistema) */
    @NotBlank(message = "Email è obbligatoria")
    @Email(message = "Email non valida")
    private String email;
    
    /** Password in plaintext (verrà verificata con hash BCrypt nel database) */
    @NotBlank(message = "Password è obbligatoria")
    private String password;
}
