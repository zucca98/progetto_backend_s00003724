package com.epicode.Progetto_Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LocatarioRequestDTO - Data Transfer Object per la creazione/aggiornamento di locatari.
 * 
 * Utilizzato negli endpoint:
 * - POST /api/locatari (creazione)
 * - PUT /api/locatari/{id} (aggiornamento)
 * 
 * Ogni locatario deve essere associato a un utente (User) esistente tramite userId.
 * Il codice fiscale deve essere univoco nel sistema.
 * 
 * Validazioni:
 * - nome, cognome: Obbligatori e non vuoti
 * - cf: Obbligatorio, non vuoto e univoco nel database
 * - indirizzo, telefono: Obbligatori e non vuoti
 * - userId: Obbligatorio (l'utente deve esistere nel database)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocatarioRequestDTO {
    
    /** Nome del locatario */
    @NotBlank(message = "Nome è obbligatorio")
    private String nome;
    
    /** Cognome del locatario */
    @NotBlank(message = "Cognome è obbligatorio")
    private String cognome;
    
    /** Codice fiscale (deve essere univoco nel database) */
    @NotBlank(message = "Codice Fiscale è obbligatorio")
    private String cf;
    
    /** Indirizzo completo del locatario */
    @NotBlank(message = "Indirizzo è obbligatorio")
    private String indirizzo;
    
    /** Numero di telefono del locatario */
    @NotBlank(message = "Telefono è obbligatorio")
    private String telefono;
    
    /** ID dell'utente associato (deve esistere nel database) */
    @NotNull(message = "User ID è obbligatorio")
    private Long userId;
}
