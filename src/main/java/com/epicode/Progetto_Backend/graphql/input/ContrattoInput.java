package com.epicode.Progetto_Backend.graphql.input;

import com.epicode.Progetto_Backend.entity.FrequenzaRata;

import lombok.Data;

/**
 * ContrattoInput - Input type GraphQL per la creazione/aggiornamento di contratti.
 * 
 * Utilizzato nelle mutation GraphQL:
 * - createContratto(input: ContrattoInput!)
 * - updateContratto(id: ID!, input: ContrattoInput!)
 * 
 * Questo Input viene convertito in ContrattoRequestDTO dal MutationResolver
 * prima di essere passato al servizio.
 * 
 * Note:
 * - dataInizio: Deve essere in formato ISO (YYYY-MM-DD) come String
 * - frequenzaRata: Enum GraphQL (MENSILE, TRIMESTRALE, etc.)
 * 
 * @see com.epicode.Progetto_Backend.graphql.MutationResolver
 * @see com.epicode.Progetto_Backend.dto.ContrattoRequestDTO
 */
@Data
public class ContrattoInput {
    /** ID del locatario (deve esistere nel database) */
    private Long locatarioId;
    
    /** ID dell'immobile (deve esistere nel database) */
    private Long immobileId;
    
    /** Data di inizio del contratto in formato ISO (YYYY-MM-DD) */
    private String dataInizio;
    
    /** Durata del contratto in anni */
    private Integer durataAnni;
    
    /** Canone annuo di affitto in euro */
    private Double canoneAnnuo;
    
    /** Frequenza di pagamento delle rate (MENSILE, TRIMESTRALE, etc.) */
    private FrequenzaRata frequenzaRata;
}






