package com.epicode.Progetto_Backend.service;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.AuthResponseDTO;
import com.epicode.Progetto_Backend.dto.LoginRequestDTO;
import com.epicode.Progetto_Backend.dto.RegisterRequestDTO;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * AuthServiceTest - Test unitari per il servizio di autenticazione.
 * 
 * Questa classe testa i metodi del AuthService, verificando:
 * - Registrazione nuovi utenti (successo, email duplicata)
 * - Login utenti (successo, credenziali errate)
 * - Generazione token JWT
 * - Assegnazione ruolo di default (ROLE_LOCATARIO) alla registrazione
 * - Gestione degli errori (BusinessException, BadCredentialsException)
 * 
 * Il MailgunService viene mockato per evitare chiamate reali all'API
 * esterna durante l'esecuzione dei test.
 * 
 * @see com.epicode.Progetto_Backend.service.AuthService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused", "ThrowableResultOfMethodCallIgnored", "removal"})
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Mock MailgunService per evitare chiamate reali all'API durante i test
    // Note: @MockBean is deprecated in Spring Boot 3.4+ but replacement @MockitoBean not available in 3.5.9
    @MockBean
    private MailgunService mailgunService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Crea ruolo LOCATARIO se non esiste
        if (roleRepository.findByName("ROLE_LOCATARIO").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_LOCATARIO").build());
        }
    }

    @Test
    void testRegister_Success() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("newuser@test.com");
        request.setPassword("Password123!");
        request.setNome("New");
        request.setCognome("User");

        AuthResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("newuser@test.com", response.getEmail());
        assertEquals("New", response.getNome());
        assertEquals("User", response.getCognome());
        assertTrue(response.getRoles().contains("ROLE_LOCATARIO"));

        // Verifica che l'utente sia stato salvato
        assertTrue(userRepository.existsByEmail("newuser@test.com"));
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testRegister_EmailAlreadyExists() {
        // Crea utente esistente
        Role role = roleRepository.findByName("ROLE_LOCATARIO").orElseThrow();
        User existingUser = User.builder()
                .email("existing@test.com")
                .password(passwordEncoder.encode("Password123!"))
                .nome("Existing")
                .cognome("User")
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        userRepository.save(existingUser);

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("existing@test.com");
        request.setPassword("Password123!");
        request.setNome("New");
        request.setCognome("User");

        assertThrows(com.epicode.Progetto_Backend.exception.BusinessException.class, 
                () -> authService.register(request));
    }

    @Test
    void testLogin_Success() {
        // Crea utente per il login
        Role role = roleRepository.findByName("ROLE_LOCATARIO").orElseThrow();
        User user = User.builder()
                .email("login@test.com")
                .password(passwordEncoder.encode("Password123!"))
                .nome("Login")
                .cognome("User")
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        userRepository.save(user);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("login@test.com");
        request.setPassword("Password123!");

        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("login@test.com", response.getEmail());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testLogin_InvalidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("nonexistent@test.com");
        request.setPassword("WrongPassword");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}

