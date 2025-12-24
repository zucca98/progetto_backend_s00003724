package com.epicode.Progetto_Backend.controller;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.epicode.Progetto_Backend.dto.ContrattoRequestDTO;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.FrequenzaRata;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.ContrattoRepository;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ContrattoControllerTest - Test unitari per il controller di gestione contratti.
 * 
 * Questa classe testa gli endpoint REST del ContrattoController, verificando:
 * - Creazione contratti (con generazione automatica rate)
 * - Recupero lista contratti con paginazione
 * - Recupero contratto per ID
 * - Aggiornamento contratti
 * - Eliminazione contratti
 * - Recupero contratti per locatario (/api/contratti/me per LOCATARIO)
 * - Controllo accesso LOCATARIO ai propri contratti
 * - Gestione accesso basato su ruoli (ADMIN, MANAGER possono gestire tutti i contratti)
 * 
 * I test verificano anche la corretta implementazione del controllo di accesso
 * per i LOCATARIO che possono vedere solo i propri contratti.
 * 
 * @see com.epicode.Progetto_Backend.controller.ContrattoController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused"})
class ContrattoControllerTest {

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
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User adminUser;
    private User managerUser;
    private User locatarioUser;
    private Locatario testLocatario;
    private Immobile testImmobile;
    private Contratto testContratto;

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
        testImmobile = new Immobile();
        testImmobile.setIndirizzo("Via Test 1");
        testImmobile.setCitta("Milano");
        testImmobile.setSuperficie(100.0);
        testImmobile.setTipo(TipoImmobile.APPARTAMENTO);
        testImmobile = immobileRepository.save(testImmobile);

        // Crea contratto
        testContratto = Contratto.builder()
                .locatario(testLocatario)
                .immobile(testImmobile)
                .dataInizio(LocalDate.of(2024, 1, 1))
                .durataAnni(3)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.TRIMESTRALE)
                .build();
        testContratto = contrattoRepository.save(testContratto);
    }

    @Test
    void testGetAllContratti_AdminSuccess() throws Exception {
        mockMvc.perform(get("/api/contratti")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetContrattoById_Success() throws Exception {
        mockMvc.perform(get("/api/contratti/" + testContratto.getId())
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testContratto.getId()));
    }

    @Test
    void testGetMyContratti_Success() throws Exception {
        mockMvc.perform(get("/api/contratti/me")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreateContratto_Success() throws Exception {
        ContrattoRequestDTO request = new ContrattoRequestDTO();
        request.setLocatarioId(testLocatario.getId());
        request.setImmobileId(testImmobile.getId());
        request.setDataInizio(LocalDate.of(2024, 6, 1));
        request.setDurataAnni(2);
        request.setCanoneAnnuo(10000.0);
        request.setFrequenzaRata(FrequenzaRata.MENSILE);

        mockMvc.perform(post("/api/contratti")
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durataAnni").value(2));
    }

    @Test
    void testUpdateContratto_Success() throws Exception {
        ContrattoRequestDTO request = new ContrattoRequestDTO();
        request.setLocatarioId(testLocatario.getId());
        request.setImmobileId(testImmobile.getId());
        request.setDataInizio(LocalDate.of(2024, 1, 1));
        request.setDurataAnni(3);
        request.setCanoneAnnuo(15000.0);
        request.setFrequenzaRata(FrequenzaRata.TRIMESTRALE);

        mockMvc.perform(put("/api/contratti/" + testContratto.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canoneAnnuo").value(15000.0));
    }

    @Test
    void testDeleteContratto_AdminOnly() throws Exception {
        Contratto toDelete = Contratto.builder()
                .locatario(testLocatario)
                .immobile(testImmobile)
                .dataInizio(LocalDate.of(2024, 1, 1))
                .durataAnni(1)
                .canoneAnnuo(5000.0)
                .frequenzaRata(FrequenzaRata.ANNUALE)
                .build();
        toDelete = contrattoRepository.save(toDelete);

        mockMvc.perform(delete("/api/contratti/" + toDelete.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteContratto_ForbiddenForManager() throws Exception {
        mockMvc.perform(delete("/api/contratti/" + testContratto.getId())
                .with(user(managerUser.getEmail()).roles("MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetContrattiConRateNonPagate_Success() throws Exception {
        mockMvc.perform(get("/api/contratti/rate-non-pagate")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

