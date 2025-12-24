package com.epicode.Progetto_Backend.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DebugLogger - Utility per il logging strutturato in formato JSON.
 * 
 * Fornisce un sistema di logging personalizzato per tracciare eventi
 * e dati durante lo sviluppo e il debugging dell'applicazione.
 * 
 * Caratteristiche:
 * - Scrive log in formato JSON su file (logs/debug.log)
 * - Crea automaticamente la directory se non esiste
 * - Ogni entry contiene: sessionId, runId, hypothesisId, location, message, data, timestamp
 * - Gestisce errori silenziosamente (non interrompe l'esecuzione)
 * 
 * Utilizzo:
 * - Principalmente utilizzato per logging di debug durante lo sviluppo
 * - Può essere disabilitato in produzione rimuovendo le chiamate
 * 
 * Nota: Gli errori di I/O vengono ignorati per non interrompere l'esecuzione dell'applicazione.
 */
public class DebugLogger {
    
    /** Path del file di log (relativo al working directory del progetto) */
    private static final String LOG_PATH = System.getProperty("user.dir") + "/logs/debug.log";
    
    /** ObjectMapper per serializzare i log in JSON */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Registra un evento di log strutturato in formato JSON.
     * 
     * Crea automaticamente la directory logs/ se non esiste e appende
     * l'entry al file di log. Ogni entry è una riga JSON separata.
     * 
     * @param sessionId ID della sessione di debug
     * @param runId ID dell'esecuzione
     * @param hypothesisId ID dell'ipotesi/test
     * @param location Posizione nel codice (es: "AuthController.java:45")
     * @param message Messaggio descrittivo dell'evento
     * @param data Mappa con dati aggiuntivi da loggare
     */
    public static void log(String sessionId, String runId, String hypothesisId, String location, String message, Map<String, Object> data) {
        try {
            Path logPath = Paths.get(LOG_PATH);
            // Crea la directory se non esiste
            Path parentDir = logPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("sessionId", sessionId);
            logEntry.put("runId", runId);
            logEntry.put("hypothesisId", hypothesisId);
            logEntry.put("location", location);
            logEntry.put("message", message);
            logEntry.put("data", data);
            logEntry.put("timestamp", System.currentTimeMillis());
            String json = objectMapper.writeValueAsString(logEntry);
            Files.write(logPath, (json + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (java.io.IOException e) {
            // Ignore logging errors (includes JsonProcessingException which extends IOException)
        }
    }
    // #endregion
}

