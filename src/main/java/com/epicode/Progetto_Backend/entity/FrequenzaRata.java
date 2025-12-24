package com.epicode.Progetto_Backend.entity;

/**
 * FrequenzaRata - Enum per la frequenza di pagamento delle rate di affitto.
 * 
 * Definisce la frequenza con cui vengono generate e pagate le rate di un contratto.
 * La frequenza determina quante rate vengono generate in un anno:
 * - MENSILE: 12 rate all'anno
 * - BIMESTRALE: 6 rate all'anno
 * - TRIMESTRALE: 4 rate all'anno (default)
 * - SEMESTRALE: 2 rate all'anno
 * - ANNUALE: 1 rata all'anno
 * 
 * Utilizzato nell'entità Contratto per definire la frequenza di pagamento.
 * Alla creazione di un contratto, le rate vengono generate automaticamente
 * in base a questa frequenza e alla durata del contratto.
 * 
 * @see com.epicode.Progetto_Backend.entity.Contratto
 */
public enum FrequenzaRata {
    /** Pagamento mensile (12 rate all'anno) */
    MENSILE,
    
    /** Pagamento bimestrale (6 rate all'anno) */
    BIMESTRALE,
    
    /** Pagamento trimestrale (4 rate all'anno) - Default */
    TRIMESTRALE,
    
    /** Pagamento semestrale (2 rate all'anno) */
    SEMESTRALE,
    
    /** Pagamento annuale (1 rata all'anno) */
    ANNUALE
}
