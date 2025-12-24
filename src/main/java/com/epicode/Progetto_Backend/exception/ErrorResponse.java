package com.epicode.Progetto_Backend.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ErrorResponse - DTO per la risposta di errore standardizzata.
 * 
 * Utilizzato da GlobalExceptionHandler per standardizzare tutte le risposte
 * di errore dell'API REST seguendo un formato consistente.
 * 
 * Segue lo standard RFC 7807 (Problem Details for HTTP APIs) semplificato.
 * 
 * Esempio di risposta JSON:
 * {
 *   "timestamp": "2025-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "User non trovato con id: 123",
 *   "path": "/api/users/123"
 * }
 * 
 * @see com.epicode.Progetto_Backend.exception.GlobalExceptionHandler
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    /** Timestamp dell'errore (formato ISO-8601) */
    private LocalDateTime timestamp;
    
    /** Codice HTTP status (es: 400, 404, 500) */
    private int status;
    
    /** Tipo/ragione dell'errore (es: "Not Found", "Bad Request", "Forbidden") */
    private String error;
    
    /** Messaggio descrittivo dell'errore */
    private String message;
    
    /** Percorso della richiesta che ha causato l'errore */
    private String path;
}
