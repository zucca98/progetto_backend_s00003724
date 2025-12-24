package com.epicode.Progetto_Backend.graphql.input;

import lombok.Data;

/**
 * LocatarioInput - Input type GraphQL per la creazione/aggiornamento di locatari.
 * 
 * Utilizzato nelle mutation GraphQL:
 * - createLocatario(input: LocatarioInput!)
 * - updateLocatario(id: ID!, input: LocatarioInput!)
 * 
 * Questo Input viene convertito in LocatarioRequestDTO dal MutationResolver
 * prima di essere passato al servizio.
 * 
 * Note:
 * - cf: Codice fiscale (deve essere univoco nel database)
 * - userId: ID dell'utente associato (deve esistere nel database)
 * 
 * @see com.epicode.Progetto_Backend.graphql.MutationResolver
 * @see com.epicode.Progetto_Backend.dto.LocatarioRequestDTO
 */
@Data
public class LocatarioInput {
    /** Nome del locatario */
    private String nome;
    
    /** Cognome del locatario */
    private String cognome;
    
    /** Codice fiscale (deve essere univoco) */
    private String cf;
    
    /** Indirizzo completo del locatario */
    private String indirizzo;
    
    /** Numero di telefono del locatario */
    private String telefono;
    
    /** ID dell'utente associato (deve esistere nel database) */
    private Long userId;
}
