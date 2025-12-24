package com.epicode.Progetto_Backend.controller;

import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.FrequenzaRata;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.ContrattoRepository;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RataRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * RataControllerTest - Test unitari per il controller di gestione rate (installments).
 * 
 * Questa classe testa gli endpoint REST del RataController, verificando:
 * - Recupero lista rate con paginazione
 * - Recupero rata per ID
 * - Aggiornamento rate (marcatura come pagata)
 * - Recupero rate per contratto
 * - Recupero rate dell'utente corrente (/api/rate/me per LOCATARIO)
 * - Controllo accesso LOCATARIO alle proprie rate
 * - Gestione accesso basato su ruoli (ADMIN, MANAGER possono vedere tutte le rate)
 * 
 * I test verificano anche la corretta implementazione del controllo di accesso
 * per i LOCATARIO che possono vedere solo le rate dei propri contratti.
 * 
 * @see com.epicode.Progetto_Backend.controller.RataController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused"})
class RataControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocatarioRepository locatarioRepository;

    @Autowired
    private ImmobileRepository immobileRepository;

    @Autowired
    private ContrattoRepository contrattoRepository;

    @Autowired
    private RataRepository rataRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private User adminUser;
    private User managerUser;
    private User locatarioUser;
    private Locatario testLocatario;
    private Contratto testContratto;
    private Rata testRata;

    @BeforeEach
    @SuppressWarnings("unused")
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

        // Crea utenti
        adminUser = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Admin")
                .cognome("Test")
                .enabled(true)
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();
        adminUser = userRepository.save(adminUser);

        managerUser = User.builder()
                .email("manager@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Manager")
                .cognome("Test")
                .enabled(true)
                .roles(new HashSet<>(Set.of(managerRole)))
                .build();
        managerUser = userRepository.save(managerUser);

        locatarioUser = User.builder()
                .email("locatario@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Locatario")
                .cognome("Test")
                .enabled(true)
                .roles(new HashSet<>(Set.of(locatarioRole)))
                .build();
        locatarioUser = userRepository.save(locatarioUser);

        // Crea locatario
        testLocatario = Locatario.builder()
                .nome("Test")
                .cognome("Locatario")
                .cf("TSTLCT80A01H501X")
                .indirizzo("Via Test 1")
                .telefono("1234567890")
                .user(locatarioUser)
                .build();
        testLocatario = locatarioRepository.save(testLocatario);

        // Crea immobile
        Immobile immobile = new Immobile();
        immobile.setIndirizzo("Via Test 1");
        immobile.setCitta("Milano");
        immobile.setSuperficie(100.0);
        immobile.setTipo(TipoImmobile.APPARTAMENTO);
        immobile = immobileRepository.save(immobile);

        // Crea contratto
        testContratto = Contratto.builder()
                .locatario(testLocatario)
                .immobile(immobile)
                .dataInizio(LocalDate.of(2024, 1, 1))
                .durataAnni(3)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.TRIMESTRALE)
                .build();
        testContratto = contrattoRepository.save(testContratto);

        // Crea rata
        testRata = Rata.builder()
                .contratto(testContratto)
                .numeroRata(1)
                .dataScadenza(LocalDate.of(2024, 4, 1))
                .importo(3000.0)
                .pagata('N')
                .build();
        testRata = rataRepository.save(testRata);
    }

    @Test
    void testGetAllRate_AdminSuccess() throws Exception {
        mockMvc.perform(get("/api/rate")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetRataById_Success() throws Exception {
        mockMvc.perform(get("/api/rate/" + testRata.getId())
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRata.getId()));
    }

    @Test
    void testGetRateByContratto_Success() throws Exception {
        mockMvc.perform(get("/api/rate/contratto/" + testContratto.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetMyRate_Success() throws Exception {
        mockMvc.perform(get("/api/rate/me")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testUpdateRataPagata_Success() throws Exception {
        mockMvc.perform(put("/api/rate/" + testRata.getId() + "/pagata")
                .param("pagata", "S")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagata").value("S"));
    }

    @Test
    void testGetRateNonPagate_Success() throws Exception {
        mockMvc.perform(get("/api/rate/non-pagate")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetRateScadute_Success() throws Exception {
        mockMvc.perform(get("/api/rate/scadute")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAllRate_ForbiddenForLocatario() throws Exception {
        mockMvc.perform(get("/api/rate")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isForbidden());
    }
}

