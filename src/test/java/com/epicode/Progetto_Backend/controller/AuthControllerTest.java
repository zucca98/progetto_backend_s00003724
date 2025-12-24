package com.epicode.Progetto_Backend.controller;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.epicode.Progetto_Backend.dto.LoginRequestDTO;
import com.epicode.Progetto_Backend.dto.RegisterRequestDTO;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AuthControllerTest - Test unitari per il controller di autenticazione.
 * 
 * Questa classe testa gli endpoint REST del AuthController, verificando:
 * - Registrazione utenti (successo, email duplicata, dati invalidi)
 * - Login utenti (successo, credenziali errate, dati invalidi)
 * - Gestione corretta delle risposte HTTP e dei messaggi di errore
 * 
 * I test utilizzano MockMvc per simulare richieste HTTP senza avviare
 * un server completo, con Spring Security abilitato per testare anche
 * i meccanismi di autenticazione.
 * 
 * Configurazione:
 * - Database H2 in-memory per isolare i test
 * - Transazioni che vengono rollbackate dopo ogni test
 * - PasswordEncoder per codificare le password degli utenti di test
 * 
 * @see com.epicode.Progetto_Backend.controller.AuthController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    /**
     * Configurazione iniziale eseguita prima di ogni test.
     * 
     * Configura MockMvc con Spring Security e crea i ruoli base
     * necessari per i test se non esistono già nel database.
     */
    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        
        // Crea ruoli se non esistono
        if (roleRepository.findByName("ROLE_LOCATARIO").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_LOCATARIO").build());
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        }
    }

    /**
     * Test della registrazione utente con dati validi.
     * 
     * Verifica che la registrazione di un nuovo utente con dati corretti
     * restituisca status 201 Created, un token JWT valido, e tutti i dati
     * dell'utente nella risposta.
     */
    @Test
    void testRegister_Success() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setNome("Test");
        request.setCognome("User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nome").value("Test"))
                .andExpect(jsonPath("$.cognome").value("User"));
    }

    /**
     * Test della registrazione con email già esistente.
     * 
     * Verifica che tentare di registrare un utente con un'email già presente
     * nel database restituisca status 400 Bad Request (o l'errore appropriato).
     */
    @Test
    void testRegister_EmailAlreadyExists() throws Exception {
        // Crea utente esistente
        Role role = roleRepository.findByName("ROLE_LOCATARIO").orElseThrow();
        User existingUser = User.builder()
                .email("existing@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .nome("Existing")
                .cognome("User")
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        userRepository.save(existingUser);

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("existing@example.com");
        request.setPassword("Password123!");
        request.setNome("New");
        request.setCognome("User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test della registrazione con dati invalidi (email non valida, password troppo corta).
     * 
     * Verifica che la validazione dei DTO funzioni correttamente e restituisca
     * status 400 Bad Request per dati che non rispettano i vincoli di validazione.
     */
    @Test
    void testRegister_InvalidData() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("invalid-email");
        request.setPassword("123");
        request.setNome("");
        request.setCognome("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test del login con credenziali valide.
     * 
     * Verifica che il login di un utente esistente con credenziali corrette
     * restituisca status 200 OK, un token JWT valido, e i dati dell'utente
     * nella risposta.
     */
    @Test
    void testLogin_Success() throws Exception {
        // Crea utente per il login
        Role role = roleRepository.findByName("ROLE_LOCATARIO").orElseThrow();
        User user = User.builder()
                .email("login@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .nome("Login")
                .cognome("User")
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        userRepository.save(user);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("login@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("login@example.com"));
    }

    /**
     * Test del login con credenziali errate (utente non esistente o password sbagliata).
     * 
     * Verifica che tentare il login con credenziali non valide restituisca
     * status 401 Unauthorized.
     */
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("nonexistent@example.com");
        request.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test del login con dati invalidi (email non valida, password vuota).
     * 
     * Verifica che la validazione dei DTO funzioni correttamente anche per il login
     * e restituisca status 400 Bad Request per dati che non rispettano i vincoli.
     */
    @Test
    void testLogin_InvalidData() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("invalid-email");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

