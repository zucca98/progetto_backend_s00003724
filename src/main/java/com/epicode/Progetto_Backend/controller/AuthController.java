package com.epicode.Progetto_Backend.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epicode.Progetto_Backend.dto.AuthResponseDTO;
import com.epicode.Progetto_Backend.dto.LoginRequestDTO;
import com.epicode.Progetto_Backend.dto.RegisterRequestDTO;
import com.epicode.Progetto_Backend.service.AuthService;
import com.epicode.Progetto_Backend.util.DebugLogger;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * AuthController - Controller REST per l'autenticazione e registrazione degli utenti.
 * 
 * Gestisce gli endpoint pubblici per:
 * - Registrazione nuovi utenti
 * - Login utenti esistenti
 * 
 * Tutti gli endpoint sono pubblici (permitAll in SecurityConfig) e non richiedono autenticazione.
 * 
 * Dopo il login/register, viene restituito un token JWT che deve essere incluso
 * nelle successive richieste nell'header: Authorization: Bearer {token}
 * 
 * @see com.epicode.Progetto_Backend.service.AuthService
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Registra un nuovo utente nel sistema.
     * 
     * Il metodo:
     * 1. Valida i dati di input (email, password, nome, cognome)
     * 2. Verifica che l'email non sia già utilizzata
     * 3. Hasha la password con BCrypt
     * 4. Crea l'utente nel database
     * 5. Genera un token JWT
     * 6. Restituisce i dati utente e il token
     * 
     * @param request DTO con email, password, nome, cognome
     * @return ResponseEntity con token JWT e dati utente (201 Created)
     * @throws jakarta.validation.ConstraintViolationException se i dati non sono validi
     * @throws org.springframework.dao.DataIntegrityViolationException se l'email esiste già
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        // #region agent log
        Map<String, Object> logData = new HashMap<>();
        logData.put("method", "POST");
        logData.put("path", "/api/auth/register");
        logData.put("email", request.getEmail());
        DebugLogger.log("debug-session", "run1", "A", "AuthController.java:22", "Register request received", logData);
        // #endregion
        try {
            AuthResponseDTO response = authService.register(request);
            // #region agent log
            Map<String, Object> logData2 = new HashMap<>();
            logData2.put("status", 201);
            logData2.put("userId", response.getUserId());
            DebugLogger.log("debug-session", "run1", "A", "AuthController.java:28", "Register success", logData2);
            // #endregion
            URI location = Objects.requireNonNull(URI.create("/api/users/" + response.getUserId()));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .location(location)
                    .body(response);
        } catch (Exception e) {
            // #region agent log
            Map<String, Object> logData2 = new HashMap<>();
            logData2.put("error", e.getMessage());
            logData2.put("errorType", e.getClass().getSimpleName());
            DebugLogger.log("debug-session", "run1", "A", "AuthController.java:35", "Register error", logData2);
            // #endregion
            throw e;
        }
    }
    
    /**
     * Autentica un utente esistente e restituisce un token JWT.
     * 
     * Il metodo:
     * 1. Valida le credenziali (email e password)
     * 2. Verifica che l'utente esista e sia abilitato (enabled = true)
     * 3. Confronta la password hashata con quella fornita (BCrypt)
     * 4. Genera un nuovo token JWT valido per 24 ore
     * 5. Restituisce i dati utente e il token
     * 
     * @param request DTO con email e password
     * @return ResponseEntity con token JWT e dati utente (200 OK)
     * @throws org.springframework.security.authentication.BadCredentialsException se credenziali non valide
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException se utente non trovato
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        // #region agent log
        Map<String, Object> logData = new HashMap<>();
        logData.put("method", "POST");
        logData.put("path", "/api/auth/login");
        logData.put("email", request.getEmail());
        DebugLogger.log("debug-session", "run1", "A", "AuthController.java:47", "Login request received", logData);
        // #endregion
        try {
            AuthResponseDTO response = authService.login(request);
            // #region agent log
            Map<String, Object> logData2 = new HashMap<>();
            logData2.put("status", 200);
            logData2.put("userId", response.getUserId());
            logData2.put("hasToken", response.getToken() != null);
            DebugLogger.log("debug-session", "run1", "A", "AuthController.java:53", "Login success", logData2);
            // #endregion
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // #region agent log
            Map<String, Object> logData2 = new HashMap<>();
            logData2.put("error", e.getMessage());
            logData2.put("errorType", e.getClass().getSimpleName());
            DebugLogger.log("debug-session", "run1", "A", "AuthController.java:60", "Login error", logData2);
            // #endregion
            throw e;
        }
    }
}
