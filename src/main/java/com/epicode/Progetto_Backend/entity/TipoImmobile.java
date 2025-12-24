package com.epicode.Progetto_Backend.entity;

/**
 * TipoImmobile - Enum per i tipi di immobili gestiti dal sistema.
 * 
 * Definisce i tipi di immobili supportati, ciascuno con caratteristiche specifiche:
 * - APPARTAMENTO: Residenziale con piano e numero di camere
 * - NEGOZIO: Commerciale con vetrine e magazzino
 * - UFFICIO: Commerciale/terziario con posti di lavoro e sale riunioni
 * 
 * Utilizzato nell'entità Immobile con ereditarietà JPA (InheritanceType.JOINED)
 * per creare tabelle separate per ogni tipo con i campi specifici.
 * 
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see com.epicode.Progetto_Backend.entity.Appartamento
 * @see com.epicode.Progetto_Backend.entity.Negozio
 * @see com.epicode.Progetto_Backend.entity.Ufficio
 */
public enum TipoImmobile {
    /** Immobile residenziale con piano e numero di camere */
    APPARTAMENTO,
    
    /** Immobile commerciale con vetrine e magazzino */
    NEGOZIO,
    
    /** Immobile commerciale/terziario con posti di lavoro e sale riunioni */
    UFFICIO
}
