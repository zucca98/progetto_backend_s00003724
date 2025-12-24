package com.epicode.Progetto_Backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ManutenzioneRequestDTO - Data Transfer Object per la creazione/aggiornamento di manutenzioni.
 * 
 * Utilizzato negli endpoint:
 * - POST /api/manutenzioni (creazione)
 * - PUT /api/manutenzioni/{id} (aggiornamento)
 * 
 * Ogni manutenzione è associata a un immobile e un locatario.
 * Il tipo di manutenzione può essere "ORDINARIA" o "STRAORDINARIA".
 * 
 * Validazioni:
 * - immobileId, locatarioId: Obbligatori (devono esistere nel database)
 * - dataMan: Obbligatoria (formato LocalDate: YYYY-MM-DD)
 * - importo: Obbligatorio e positivo (in euro)
 * - tipo: Default "STRAORDINARIA" (opzionale, può essere "ORDINARIA" o "STRAORDINARIA")
 * - descrizione: Opzionale (descrizione dettagliata della manutenzione)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManutenzioneRequestDTO {
    
    /** ID dell'immobile su cui è stata effettuata la manutenzione (deve esistere nel database) */
    @NotNull(message = "Immobile ID è obbligatorio")
    private Long immobileId;
    
    /** ID del locatario che ha richiesto/effettuato la manutenzione (deve esistere nel database) */
    @NotNull(message = "Locatario ID è obbligatorio")
    private Long locatarioId;
    
    /** Data in cui è stata effettuata la manutenzione (formato: YYYY-MM-DD) */
    @NotNull(message = "Data manutenzione è obbligatoria")
    private LocalDate dataMan;
    
    /** Importo della manutenzione in euro */
    @NotNull(message = "Importo è obbligatorio")
    @Positive(message = "Importo deve essere positivo")
    private Double importo;
    
    /** Tipo di manutenzione: "ORDINARIA" o "STRAORDINARIA" (default: "STRAORDINARIA") */
    @Builder.Default
    private String tipo = "STRAORDINARIA";
    
    /** Descrizione dettagliata della manutenzione (opzionale) */
    private String descrizione;
}
