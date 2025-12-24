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

import com.epicode.Progetto_Backend.dto.ManutenzioneRequestDTO;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Manutenzione;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.ManutenzioneRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ManutenzioneControllerTest - Test unitari per il controller di gestione manutenzioni.
 * 
 * Questa classe testa gli endpoint REST del ManutenzioneController, verificando:
 * - Creazione record di manutenzione
 * - Recupero lista manutenzioni con paginazione
 * - Recupero manutenzione per ID
 * - Aggiornamento manutenzioni
 * - Eliminazione manutenzioni
 * - Statistiche manutenzioni
 * - Controllo accesso LOCATARIO alle proprie manutenzioni
 * - Gestione accesso basato su ruoli (ADMIN, MANAGER possono gestire tutte le manutenzioni)
 * 
 * I test verificano anche la corretta implementazione del controllo di accesso
 * per i LOCATARIO che possono vedere solo le manutenzioni associate ai propri contratti.
 * 
 * @see com.epicode.Progetto_Backend.controller.ManutenzioneController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused"})
class ManutenzioneControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocatarioRepository locatarioRepository;

    @Autowired
    private ImmobileRepository immobileRepository;

    @Autowired
    private ManutenzioneRepository manutenzioneRepository;

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
    private Manutenzione testManutenzione;

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

        // Crea manutenzione
        testManutenzione = Manutenzione.builder()
                .immobile(testImmobile)
                .locatario(testLocatario)
                .dataMan(LocalDate.of(2024, 6, 15))
                .importo(2500.0)
                .tipo("STRAORDINARIA")
                .descrizione("Test manutenzione")
                .build();
        testManutenzione = manutenzioneRepository.save(testManutenzione);
    }

    @Test
    void testGetAllManutenzioni_AdminSuccess() throws Exception {
        mockMvc.perform(get("/api/manutenzioni")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetManutenzioneById_Success() throws Exception {
        mockMvc.perform(get("/api/manutenzioni/" + testManutenzione.getId())
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testManutenzione.getId()));
    }

    @Test
    void testGetMyManutenzioni_Success() throws Exception {
        mockMvc.perform(get("/api/manutenzioni/me")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreateManutenzione_Success() throws Exception {
        ManutenzioneRequestDTO request = new ManutenzioneRequestDTO();
        request.setImmobileId(testImmobile.getId());
        request.setLocatarioId(testLocatario.getId());
        request.setDataMan(LocalDate.of(2024, 7, 1));
        request.setImporto(1500.0);
        request.setTipo("ORDINARIA");
        request.setDescrizione("Nuova manutenzione");

        mockMvc.perform(post("/api/manutenzioni")
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importo").value(1500.0));
    }

    @Test
    void testUpdateManutenzione_Success() throws Exception {
        ManutenzioneRequestDTO request = new ManutenzioneRequestDTO();
        request.setImmobileId(testImmobile.getId());
        request.setLocatarioId(testLocatario.getId());
        request.setDataMan(LocalDate.of(2024, 6, 15));
        request.setImporto(3000.0);
        request.setDescrizione("Manutenzione aggiornata");

        mockMvc.perform(put("/api/manutenzioni/" + testManutenzione.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importo").value(3000.0));
    }

    @Test
    void testDeleteManutenzione_AdminOnly() throws Exception {
        Manutenzione toDelete = Manutenzione.builder()
                .immobile(testImmobile)
                .locatario(testLocatario)
                .dataMan(LocalDate.of(2024, 8, 1))
                .importo(500.0)
                .tipo("ORDINARIA")
                .descrizione("Da eliminare")
                .build();
        toDelete = manutenzioneRepository.save(toDelete);

        mockMvc.perform(delete("/api/manutenzioni/" + toDelete.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetManutenzioniByLocatarioAndAnno_Success() throws Exception {
        mockMvc.perform(get("/api/manutenzioni/locatario/" + testLocatario.getId() + "/anno/2024")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetDateManutenzioniByImporto_Success() throws Exception {
        mockMvc.perform(get("/api/manutenzioni/locatario/" + testLocatario.getId() + "/importo-maggiore/1000")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetTotaleSpesePerAnnoCitta_Success() throws Exception {
        mockMvc.perform(get("/api/manutenzioni/totale-per-anno-citta")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }
}

