package com.epicode.Progetto_Backend.graphql.input;

import lombok.Data;

/**
 * ManutenzioneInput - Input type GraphQL per la creazione/aggiornamento di manutenzioni.
 * 
 * Utilizzato nelle mutation GraphQL:
 * - createManutenzione(input: ManutenzioneInput!)
 * - updateManutenzione(id: ID!, input: ManutenzioneInput!)
 * 
 * Questo Input viene convertito in ManutenzioneRequestDTO dal MutationResolver
 * prima di essere passato al servizio.
 * 
 * Note:
 * - dataMan: Deve essere in formato ISO (YYYY-MM-DD) come String
 * - tipo: "ORDINARIA" o "STRAORDINARIA" (default nel DTO: "STRAORDINARIA")
 * - descrizione: Opzionale
 * 
 * @see com.epicode.Progetto_Backend.graphql.MutationResolver
 * @see com.epicode.Progetto_Backend.dto.ManutenzioneRequestDTO
 */
@Data
public class ManutenzioneInput {
    /** ID dell'immobile (deve esistere nel database) */
    private Long immobileId;
    
    /** ID del locatario (deve esistere nel database) */
    private Long locatarioId;
    
    /** Data della manutenzione in formato ISO (YYYY-MM-DD) */
    private String dataMan;
    
    /** Importo della manutenzione in euro */
    private Double importo;
    
    /** Tipo di manutenzione: "ORDINARIA" o "STRAORDINARIA" */
    private String tipo;
    
    /** Descrizione dettagliata della manutenzione (opzionale) */
    private String descrizione;
}
