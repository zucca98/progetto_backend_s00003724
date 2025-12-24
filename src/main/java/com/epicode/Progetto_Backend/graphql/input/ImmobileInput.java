package com.epicode.Progetto_Backend.graphql.input;

import com.epicode.Progetto_Backend.entity.TipoImmobile;

import lombok.Data;

/**
 * ImmobileInput - Input type GraphQL per la creazione/aggiornamento di immobili.
 * 
 * Utilizzato nelle mutation GraphQL:
 * - createImmobile(input: ImmobileInput!)
 * - updateImmobile(id: ID!, input: ImmobileInput!)
 * 
 * Questo Input viene convertito in ImmobileRequestDTO dal MutationResolver
 * prima di essere passato al servizio.
 * 
 * Il tipo di immobile determina quali campi sono utilizzati:
 * - APPARTAMENTO: piano, numCamere
 * - NEGOZIO: vetrine, magazzinoMq
 * - UFFICIO: postiLavoro, saleRiunioni
 * 
 * @see com.epicode.Progetto_Backend.graphql.MutationResolver
 * @see com.epicode.Progetto_Backend.dto.ImmobileRequestDTO
 * @see com.epicode.Progetto_Backend.entity.TipoImmobile
 */
@Data
public class ImmobileInput {
    /** Indirizzo completo dell'immobile */
    private String indirizzo;
    
    /** Città dell'immobile */
    private String citta;
    
    /** Superficie totale in metri quadri */
    private Double superficie;
    
    /** Tipo di immobile (APPARTAMENTO, NEGOZIO, UFFICIO) */
    private TipoImmobile tipo;
    
    // Campi per Appartamento
    /** Piano dell'appartamento (richiesto se tipo = APPARTAMENTO) */
    private Integer piano;
    
    /** Numero di camere (richiesto se tipo = APPARTAMENTO) */
    private Integer numCamere;
    
    // Campi per Ufficio
    /** Numero di posti di lavoro (richiesto se tipo = UFFICIO) */
    private Integer postiLavoro;
    
    /** Numero di sale riunioni (richiesto se tipo = UFFICIO) */
    private Integer saleRiunioni;
    
    // Campi per Negozio
    /** Numero di vetrine (richiesto se tipo = NEGOZIO) */
    private Integer vetrine;
    
    /** Metri quadri del magazzino (richiesto se tipo = NEGOZIO) */
    private Double magazzinoMq;
}
