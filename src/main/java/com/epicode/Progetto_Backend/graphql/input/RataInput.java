package com.epicode.Progetto_Backend.graphql.input;

import lombok.Data;

/**
 * RataInput - Input type GraphQL per la creazione/aggiornamento di rate.
 * 
 * Utilizzato nelle mutation GraphQL:
 * - createRata(input: RataInput!)
 * - updateRata(id: ID!, input: RataInput!)
 * 
 * Questo Input viene convertito in RataRequestDTO dal MutationResolver
 * prima di essere passato al servizio.
 * 
 * Note:
 * - dataScadenza: Deve essere in formato ISO (YYYY-MM-DD) come String
 * - pagata: Boolean che viene convertito in Character ('S'/'N') nel DTO
 * - Nota: Le rate vengono generalmente generate automaticamente alla creazione di un contratto
 * 
 * @see com.epicode.Progetto_Backend.graphql.MutationResolver
 * @see com.epicode.Progetto_Backend.dto.RataRequestDTO
 */
@Data
public class RataInput {
    /** ID del contratto (deve esistere nel database) */
    private Long contrattoId;
    
    /** Numero progressivo della rata nel contratto */
    private Integer numeroRata;
    
    /** Data di scadenza in formato ISO (YYYY-MM-DD) */
    private String dataScadenza;
    
    /** Importo della rata in euro */
    private Double importo;
    
    /** Stato pagamento come Boolean (viene convertito in Character 'S'/'N') */
    private Boolean pagata;
}
