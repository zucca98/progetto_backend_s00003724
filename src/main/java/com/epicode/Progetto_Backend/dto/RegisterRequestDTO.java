package com.epicode.Progetto_Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RegisterRequestDTO - Data Transfer Object per la richiesta di registrazione.
 * 
 * Utilizzato nell'endpoint POST /api/auth/register per registrare un nuovo utente.
 * 
 * Il DTO contiene tutti i dati necessari per creare un nuovo utente nel sistema.
 * La password verrà hashata con BCrypt prima di essere salvata nel database.
 * 
 * Validazioni:
 * - email: Deve essere obbligatoria, in formato email valido e univoca
 * - password: Deve essere obbligatoria e avere almeno 6 caratteri
 * - nome: Deve essere obbligatorio
 * - cognome: Deve essere obbligatorio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {
    
    /** Email dell'utente (deve essere univoca nel sistema) */
    @NotBlank(message = "Email è obbligatoria")
    @Email(message = "Email non valida")
    private String email;
    
    /** Password in plaintext (verrà hashata con BCrypt prima del salvataggio, minimo 6 caratteri) */
    @NotBlank(message = "Password è obbligatoria")
    @Size(min = 6, message = "Password deve avere almeno 6 caratteri")
    private String password;
    
    /** Nome dell'utente */
    @NotBlank(message = "Nome è obbligatorio")
    private String nome;
    
    /** Cognome dell'utente */
    @NotBlank(message = "Cognome è obbligatorio")
    private String cognome;
}
