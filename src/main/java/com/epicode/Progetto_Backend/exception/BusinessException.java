package com.epicode.Progetto_Backend.exception;

/**
 * BusinessException - Eccezione personalizzata per errori di business logic.
 * 
 * Utilizzata per rappresentare errori legati alle regole di business dell'applicazione
 * che non sono errori di validazione o errori tecnici.
 * 
 * Esempi di utilizzo:
 * - Tentativo di creare un contratto per un immobile già affittato
 * - Tentativo di eliminare un'entità con dipendenze
 * - Operazioni non permesse per lo stato corrente dell'entità
 * 
 * Viene gestita da GlobalExceptionHandler che restituisce un HTTP 400 Bad Request.
 * 
 * @see com.epicode.Progetto_Backend.exception.GlobalExceptionHandler
 */
public class BusinessException extends RuntimeException {
    
    /** Codice errore opzionale per identificare il tipo di errore business */
    private final String errorCode;
    
    /**
     * Costruttore con solo messaggio.
     * 
     * @param message Messaggio descrittivo dell'errore
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = null;
    }
    
    /**
     * Costruttore con messaggio e codice errore.
     * 
     * @param message Messaggio descrittivo dell'errore
     * @param errorCode Codice identificativo dell'errore (es: "CONTRACT_ALREADY_ACTIVE")
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Costruttore con messaggio e causa.
     * 
     * @param message Messaggio descrittivo dell'errore
     * @param cause Eccezione causa di questo errore
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }
    
    /**
     * Costruttore completo con messaggio, codice errore e causa.
     * 
     * @param message Messaggio descrittivo dell'errore
     * @param errorCode Codice identificativo dell'errore
     * @param cause Eccezione causa di questo errore
     */
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Restituisce il codice errore associato all'eccezione.
     * 
     * @return Codice errore o null se non specificato
     */
    public String getErrorCode() {
        return errorCode;
    }
}

