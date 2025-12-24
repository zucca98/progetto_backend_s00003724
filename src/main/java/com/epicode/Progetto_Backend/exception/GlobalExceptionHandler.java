package com.epicode.Progetto_Backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.epicode.Progetto_Backend.util.DebugLogger;

import jakarta.servlet.http.HttpServletRequest;

/**
 * GlobalExceptionHandler - Handler globale per la gestione centralizzata delle eccezioni.
 * 
 * Intercetta tutte le eccezioni non gestite nell'applicazione e restituisce
 * risposte HTTP standardizzate con formato JSON consistente.
 * 
 * Gestisce i seguenti tipi di eccezioni:
 * - MethodArgumentNotValidException: Errori di validazione Jakarta Validation (@Valid)
 * - AccessDeniedException: Accesso negato (403 Forbidden)
 * - AuthenticationException: Autenticazione mancante (401 Unauthorized)
 * - BadCredentialsException: Credenziali non valide (401 Unauthorized)
 * - EntityNotFoundException: Entità non trovata (404 Not Found)
 * - BusinessException: Errori di business logic (400 Bad Request)
 * - ValidationException: Errori di validazione personalizzati (400 Bad Request)
 * - RuntimeException: Errori runtime generici (400/500)
 * - Exception: Tutte le altre eccezioni (500 Internal Server Error)
 * 
 * Per sicurezza, i dettagli interni delle eccezioni non vengono esposti al client
 * in produzione, ma vengono loggati per il debugging.
 * 
 * @see com.epicode.Progetto_Backend.exception.ErrorResponse
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestisce gli errori di validazione Jakarta Validation (@Valid).
     * 
     * Viene chiamato quando un DTO con @Valid non supera le validazioni.
     * Restituisce una mappa dettagliata degli errori per campo.
     * 
     * @param ex Eccezione di validazione
     * @param request Richiesta HTTP che ha causato l'errore
     * @return ResponseEntity con HTTP 400 Bad Request e mappa errori
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        // #region agent log
        Map<String, Object> logData = new HashMap<>();
        logData.put("exception", "MethodArgumentNotValidException");
        logData.put("path", request.getRequestURI());
        logData.put("status", HttpStatus.BAD_REQUEST.value());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logData.put("validationErrors", errors);
        DebugLogger.log("debug-session", "run1", "B", "GlobalExceptionHandler.java:45", "Validation error", logData);
        // #endregion

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        response.put("errors", errors);
        response.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Gestisce gli errori di accesso negato (autorizzazione fallita).
     * 
     * Viene chiamato quando un utente autenticato non ha i permessi per accedere
     * a una risorsa (es: LOCATARIO che tenta di accedere a risorse di altri).
     * 
     * @param ex Eccezione di accesso negato
     * @param request Richiesta HTTP che ha causato l'errore
     * @return ResponseEntity con HTTP 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        // #region agent log
        Map<String, Object> logData = new HashMap<>();
        logData.put("exception", "AccessDeniedException");
        logData.put("path", request.getRequestURI());
        logData.put("status", HttpStatus.FORBIDDEN.value());
        logData.put("message", ex.getMessage());
        DebugLogger.log("debug-session", "run1", "C", "GlobalExceptionHandler.java:66", "Access denied", logData);
        // #endregion

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("Non hai i permessi per accedere a questa risorsa. Ruolo richiesto non presente.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Gestisce gli errori di autenticazione (utente non autenticato).
     * 
     * Viene chiamato quando un endpoint richiede autenticazione ma il token
     * non è presente o non è valido.
     * 
     * @param ex Eccezione di autenticazione
     * @param request Richiesta HTTP che ha causato l'errore
     * @return ResponseEntity con HTTP 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Autenticazione richiesta. Effettua il login per accedere.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Gestisce gli errori di credenziali non valide.
     * 
     * Viene chiamato quando email o password sono errate durante il login.
     * 
     * @param ex Eccezione di credenziali non valide
     * @param request Richiesta HTTP che ha causato l'errore
     * @return ResponseEntity con HTTP 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Email o password non validi")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Gestisce gli errori di entità non trovata.
     * 
     * Viene chiamato quando si tenta di accedere a un'entità che non esiste nel database.
     * 
     * @param ex Eccezione di entità non trovata
     * @param request Richiesta HTTP che ha causato l'errore
     * @return ResponseEntity con HTTP 404 Not Found
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Gestisce gli errori di business logic.
     * 
     * Viene chiamato quando si verifica un errore legato alle regole di business
     * dell'applicazione (es: tentativo di creare un contratto per immobile già affittato).
     * 
     * @param ex Eccezione di business logic
     * @param request Richiesta HTTP che ha causato l'errore
     * @return ResponseEntity con HTTP 400 Bad Request
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Business Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Gestisce gli errori di validazione personalizzati.
     * 
     * Viene chiamato quando si verifica un ValidationException con validazioni
     * personalizzate non coperte da Jakarta Validation.
     * 
     * @param ex Eccezione di validazione personalizzata
     * @param request Richiesta HTTP che ha causato l'errore
     * @return ResponseEntity con HTTP 400 Bad Request e mappa errori (se presente)
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        response.put("message", ex.getMessage());
        if (ex.getValidationErrors() != null) {
            response.put("errors", ex.getValidationErrors());
        }
        response.put("path", request.getRequestURI());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Gestisce le RuntimeException generiche.
     * 
     * Handler catch-all per tutte le RuntimeException non gestite da handler specifici.
     * Se l'eccezione è una BusinessException o ValidationException già gestita,
     * viene usato HTTP 400, altrimenti HTTP 500.
     * 
     * Per sicurezza, i dettagli interni vengono loggati ma non esposti al client.
     * 
     * @param ex Eccezione runtime
     * @param request Richiesta HTTP che ha causato l'errore
     * @return ResponseEntity con HTTP 400 o 500
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        
        // Log dettagliato per debugging interno
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logger.error("RuntimeException occurred at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        // Determina status code appropriato: se è un'eccezione di business, usa 400, altrimenti 500
        HttpStatus status = (ex instanceof BusinessException || ex instanceof ValidationException) 
                ? HttpStatus.BAD_REQUEST 
                : HttpStatus.INTERNAL_SERVER_ERROR;
        
        // Sanitizza messaggio: non esporre dettagli interni in produzione
        String message = (status == HttpStatus.INTERNAL_SERVER_ERROR) 
                ? "Si è verificato un errore interno. Contattare l'amministratore." 
                : ex.getMessage();
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(status).body(error);
    }
    
    /**
     * Gestisce tutte le altre eccezioni non gestite (catch-all finale).
     * 
     * Handler finale che cattura tutte le eccezioni non gestite dagli altri handler.
     * Restituisce sempre HTTP 500 Internal Server Error con messaggio generico.
     * 
     * I dettagli vengono loggati per il debugging ma non esposti al client per sicurezza.
     * 
     * @param ex Eccezione generica
     * @param request Richiesta HTTP che ha causato l'errore
     * @return ResponseEntity con HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        // #region agent log
        Map<String, Object> logData = new HashMap<>();
        logData.put("exception", ex.getClass().getSimpleName());
        logData.put("path", request.getRequestURI());
        logData.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        logData.put("message", ex.getMessage());
        logData.put("errorType", ex.getClass().getName());
        DebugLogger.log("debug-session", "run1", "D", "GlobalExceptionHandler.java:130", "Generic exception", logData);
        // #endregion
        
        // Log dettagliato per debugging interno (non esporre al client)
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logger.error("Unhandled exception occurred at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        // Sanitizza messaggio: non esporre dettagli interni del server
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Si è verificato un errore interno. Contattare l'amministratore.")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
