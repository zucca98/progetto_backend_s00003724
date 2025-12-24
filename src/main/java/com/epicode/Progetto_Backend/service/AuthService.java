package com.epicode.Progetto_Backend.service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.AuthResponseDTO;
import com.epicode.Progetto_Backend.dto.LoginRequestDTO;
import com.epicode.Progetto_Backend.dto.RegisterRequestDTO;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.exception.BusinessException;
import com.epicode.Progetto_Backend.exception.EntityNotFoundException;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;
import com.epicode.Progetto_Backend.security.JwtTokenProvider;

/**
 * AuthService - Servizio per l'autenticazione e registrazione degli utenti.
 * 
 * Gestisce le operazioni di autenticazione:
 * - Registrazione nuovi utenti con assegnazione ruolo ROLE_LOCATARIO di default
 * - Login utenti esistenti con validazione credenziali
 * - Generazione token JWT per autenticazione stateless
 * - Invio email di benvenuto (asincrono) dopo la registrazione
 * 
 * Flusso registrazione:
 * 1. Verifica che l'email non sia già registrata
 * 2. Hasha la password con BCrypt
 * 3. Crea l'utente con ruolo ROLE_LOCATARIO
 * 4. Invia email di benvenuto (asincrono)
 * 5. Genera token JWT
 * 6. Restituisce AuthResponseDTO con token e dati utente
 * 
 * Flusso login:
 * 1. Autentica le credenziali tramite AuthenticationManager
 * 2. Carica l'utente dal database
 * 3. Genera nuovo token JWT
 * 4. Restituisce AuthResponseDTO con token e dati utente
 * 
 * @see com.epicode.Progetto_Backend.security.JwtTokenProvider
 * @see com.epicode.Progetto_Backend.service.MailgunService
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private MailgunService mailgunService;
    
    /**
     * Registra un nuovo utente nel sistema.
     * 
     * Processo:
     * 1. Verifica che l'email non sia già registrata
     * 2. Hasha la password con BCrypt
     * 3. Crea l'utente con ruolo ROLE_LOCATARIO di default
     * 4. Salva l'utente nel database
     * 5. Invia email di benvenuto (asincrono, non blocca la registrazione)
     * 6. Genera token JWT
     * 7. Restituisce AuthResponseDTO con token e dati utente
     * 
     * @param request DTO con dati di registrazione (email, password, nome, cognome)
     * @return AuthResponseDTO con token JWT e dati utente
     * @throws BusinessException se l'email è già registrata
     * @throws EntityNotFoundException se il ruolo ROLE_LOCATARIO non esiste
     */
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        logger.info("Tentativo di registrazione per email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Tentativo di registrazione con email già esistente: {}", request.getEmail());
            throw new BusinessException("Email già registrata", "EMAIL_ALREADY_EXISTS");
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nome(request.getNome())
                .cognome(request.getCognome())
                .enabled(true)
                .build();
        
        // Assegna ruolo ROLE_LOCATARIO di default
        Role locatarioRole = roleRepository.findByName("ROLE_LOCATARIO")
                .orElseThrow(() -> {
                    logger.error("Ruolo ROLE_LOCATARIO non trovato nel database");
                    return new EntityNotFoundException("Role", "ROLE_LOCATARIO");
                });
        Set<Role> roles = new HashSet<>();
        roles.add(locatarioRole);
        user.setRoles(roles);
        
        userRepository.save(user);
        logger.info("Utente registrato con successo. ID: {}, Email: {}", user.getId(), user.getEmail());
        
        // Invia email di benvenuto (asincrono)
        String fullName = user.getNome() + " " + user.getCognome();
        mailgunService.sendWelcomeEmail(user.getEmail(), fullName);
        
        String token = jwtTokenProvider.generateToken(user.getEmail());
        
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .nome(user.getNome())
                .cognome(user.getCognome())
                .roles(roleNames)
                .build();
    }

    /**
     * Autentica un utente esistente e restituisce un token JWT.
     * 
     * Processo:
     * 1. Autentica le credenziali tramite AuthenticationManager (verifica password con BCrypt)
     * 2. Carica l'utente completo dal database
     * 3. Genera nuovo token JWT valido per 24 ore
     * 4. Restituisce AuthResponseDTO con token e dati utente
     * 
     * @param request DTO con credenziali (email, password)
     * @return AuthResponseDTO con token JWT e dati utente
     * @throws AuthenticationException se le credenziali non sono valide
     * @throws EntityNotFoundException se l'utente non viene trovato dopo l'autenticazione
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        logger.info("Tentativo di login per email: {}", request.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        logger.error("Utente non trovato dopo autenticazione: {}", request.getEmail());
                        return new EntityNotFoundException("User", request.getEmail());
                    });

            String token = jwtTokenProvider.generateToken(authentication.getName());
            logger.info("Login effettuato con successo per email: {}, ID: {}", user.getEmail(), user.getId());

            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            return AuthResponseDTO.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .nome(user.getNome())
                    .cognome(user.getCognome())
                    .roles(roleNames)
                    .build();
        } catch (AuthenticationException | EntityNotFoundException e) {
            logger.error("Errore durante il login per email: {}", request.getEmail(), e);
            throw e;
        }
    }
}
