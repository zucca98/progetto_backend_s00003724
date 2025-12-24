package com.epicode.Progetto_Backend.security;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * SecurityTest - Test per la configurazione e il funzionamento di Spring Security.
 * 
 * Questa classe verifica che la configurazione di sicurezza dell'applicazione
 * funzioni correttamente, testando:
 * - Accesso consentito/negato per ruolo (ADMIN, MANAGER, LOCATARIO)
 * - Protezione degli endpoint amministrativi
 * - Protezione degli endpoint manager
 * - Accesso pubblico per endpoint comuni
 * - Accesso negato per utenti non autenticati
 * - Verifica che i LOCATARIO possano accedere solo ai propri dati
 * 
 * I test utilizzano MockMvc con Spring Security abilitato per simulare
 * richieste HTTP autenticate con diversi ruoli e verificare le risposte
 * del sistema di sicurezza.
 * 
 * @see com.epicode.Progetto_Backend.security.SecurityConfig
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({ "null", "unused" })
class SecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LocatarioRepository locatarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private User adminUser;
    private User managerUser;
    private User locatarioUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Crea ruoli
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_MANAGER").build()));
        Role locatarioRole = roleRepository.findByName("ROLE_LOCATARIO")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_LOCATARIO").build()));

        // Crea admin user
        adminUser = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Admin")
                .cognome("Test")
                .enabled(true)
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();
        adminUser = userRepository.save(adminUser);

        // Crea manager user
        managerUser = User.builder()
                .email("manager@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Manager")
                .cognome("Test")
                .enabled(true)
                .roles(new HashSet<>(Set.of(managerRole)))
                .build();
        managerUser = userRepository.save(managerUser);

        // Crea locatario user
        locatarioUser = User.builder()
                .email("locatario@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Locatario")
                .cognome("Test")
                .enabled(true)
                .roles(new HashSet<>(Set.of(locatarioRole)))
                .build();
        locatarioUser = userRepository.save(locatarioUser);

        // Crea locatario associato
        Locatario testLocatario = Locatario.builder()
                .nome("Test")
                .cognome("Locatario")
                .cf("TSTLCT80A01H501X")
                .indirizzo("Via Test 1")
                .telefono("1234567890")
                .user(locatarioUser)
                .build();
        locatarioRepository.save(testLocatario);
    }


    @Test
    void testAdminCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void testManagerCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(user(managerUser.getEmail()).roles("MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLocatarioCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedCannotAccessProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminCanAccessManagerEndpoints() throws Exception {
        mockMvc.perform(get("/api/locatari")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void testManagerCanAccessManagerEndpoints() throws Exception {
        mockMvc.perform(get("/api/locatari")
                .with(user(managerUser.getEmail()).roles("MANAGER")))
                .andExpect(status().isOk());
    }

    @Test
    void testLocatarioCanAccessPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/immobili")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk());
    }

    @Test
    void testLocatarioCanAccessOwnData() throws Exception {
        mockMvc.perform(get("/api/locatari/me")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk());
    }
}

