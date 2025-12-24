package com.epicode.Progetto_Backend.exception;

import java.util.Map;

/**
 * ValidationException - Eccezione personalizzata per errori di validazione.
 * 
 * Utilizzata per rappresentare errori di validazione personalizzati che non
 * sono gestiti dalle annotazioni standard di Jakarta Validation.
 * 
 * Può contenere una mappa di errori di validazione per campo, permettendo
 * di restituire errori multipli in una sola risposta.
 * 
 * Esempi di utilizzo:
 * - Validazione business logic complessa non coperta da @Valid
 * - Validazione cross-field (es: dataFine > dataInizio)
 * - Validazione che richiede accesso al database
 * 
 * Viene gestita da GlobalExceptionHandler che restituisce un HTTP 400 Bad Request
 * con la mappa degli errori di validazione.
 * 
 * @see com.epicode.Progetto_Backend.exception.GlobalExceptionHandler
 */
public class ValidationException extends RuntimeException {
    
    /** Mappa degli errori di validazione: nome campo -> messaggio errore */
    private final Map<String, String> validationErrors;
    
    /**
     * Costruttore con solo messaggio.
     * 
     * @param message Messaggio descrittivo dell'errore di validazione
     */
    public ValidationException(String message) {
        super(message);
        this.validationErrors = null;
    }
    
    /**
     * Costruttore con messaggio e mappa di errori per campo.
     * 
     * @param message Messaggio descrittivo generale dell'errore
     * @param validationErrors Mappa di errori per campo (nome campo -> messaggio errore)
     */
    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }
    
    /**
     * Restituisce la mappa degli errori di validazione per campo.
     * 
     * @return Mappa di errori (nome campo -> messaggio) o null se non specificata
     */
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}

