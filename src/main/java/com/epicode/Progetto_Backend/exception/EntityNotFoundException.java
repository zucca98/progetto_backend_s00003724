package com.epicode.Progetto_Backend.exception;

/**
 * EntityNotFoundException - Eccezione personalizzata per entità non trovate.
 * 
 * Utilizzata quando un'entità richiesta non viene trovata nel database.
 * 
 * Esempi di utilizzo:
 * - Tentativo di recuperare un utente con ID inesistente
 * - Tentativo di aggiornare un contratto che non esiste
 * - Ricerca di un'entità per email/identificatore che non esiste
 * 
 * Viene gestita da GlobalExceptionHandler che restituisce un HTTP 404 Not Found.
 * 
 * @see com.epicode.Progetto_Backend.exception.GlobalExceptionHandler
 */
public class EntityNotFoundException extends RuntimeException {
    
    /** Nome dell'entità non trovata (es: "User", "Contratto") */
    private final String entityName;
    
    /** ID dell'entità non trovata (null se cercata per altro identificatore) */
    private final Long entityId;
    
    /**
     * Costruttore con nome entità e ID.
     * 
     * @param entityName Nome dell'entità (es: "User")
     * @param entityId ID dell'entità non trovata
     */
    public EntityNotFoundException(String entityName, Long entityId) {
        super(String.format("%s non trovato con id: %d", entityName, entityId));
        this.entityName = entityName;
        this.entityId = entityId;
    }
    
    /**
     * Costruttore con nome entità e identificatore generico (es: email, codice fiscale).
     * 
     * @param entityName Nome dell'entità (es: "User")
     * @param identifier Identificatore utilizzato per la ricerca (es: email, cf)
     */
    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s non trovato con identificatore: %s", entityName, identifier));
        this.entityName = entityName;
        this.entityId = null;
    }
    
    /**
     * Costruttore con solo messaggio personalizzato.
     * 
     * @param message Messaggio personalizzato dell'errore
     */
    public EntityNotFoundException(String message) {
        super(message);
        this.entityName = null;
        this.entityId = null;
    }
    
    /**
     * Restituisce il nome dell'entità non trovata.
     * 
     * @return Nome dell'entità o null se non specificato
     */
    public String getEntityName() {
        return entityName;
    }
    
    /**
     * Restituisce l'ID dell'entità non trovata.
     * 
     * @return ID dell'entità o null se cercata per altro identificatore
     */
    public Long getEntityId() {
        return entityId;
    }
}

