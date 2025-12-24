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

import com.epicode.Progetto_Backend.dto.ImmobileRequestDTO;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ImmobileControllerTest - Test unitari per il controller di gestione immobili.
 * 
 * Questa classe testa gli endpoint REST del ImmobileController, verificando:
 * - Creazione immobili (Appartamento, Negozio, Ufficio)
 * - Recupero lista immobili con paginazione
 * - Recupero immobile per ID
 * - Aggiornamento immobili
 * - Eliminazione immobili (solo ADMIN)
 * - Statistiche immobili
 * - Gestione accesso basato su ruoli (ADMIN, MANAGER possono creare/modificare)
 * 
 * I test verificano la corretta gestione dei diversi tipi di immobili
 * (ereditarietà) e le autorizzazioni per le operazioni CRUD.
 * 
 * @see com.epicode.Progetto_Backend.controller.ImmobileController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused"})
class ImmobileControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImmobileRepository immobileRepository;

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
    private Immobile testImmobile;

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

        // Crea immobile di test
        testImmobile = new Immobile();
        testImmobile.setIndirizzo("Via Test 1");
        testImmobile.setCitta("Milano");
        testImmobile.setSuperficie(100.0);
        testImmobile.setTipo(TipoImmobile.APPARTAMENTO);
        testImmobile = immobileRepository.save(testImmobile);
    }

    @Test
    void testGetAllImmobili_Success() throws Exception {
        mockMvc.perform(get("/api/immobili")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetImmobileById_Success() throws Exception {
        mockMvc.perform(get("/api/immobili/" + testImmobile.getId())
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testImmobile.getId()))
                .andExpect(jsonPath("$.indirizzo").value("Via Test 1"));
    }

    @Test
    void testCreateAppartamento_Success() throws Exception {
        ImmobileRequestDTO request = new ImmobileRequestDTO();
        request.setIndirizzo("Via Nuova 2");
        request.setCitta("Milano");
        request.setSuperficie(85.5);
        request.setTipo(TipoImmobile.APPARTAMENTO);
        request.setPiano(3);
        request.setNumCamere(3);

        mockMvc.perform(post("/api/immobili")
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.indirizzo").value("Via Nuova 2"))
                .andExpect(jsonPath("$.tipo").value("APPARTAMENTO"));
    }

    @Test
    void testCreateNegozio_Success() throws Exception {
        ImmobileRequestDTO request = new ImmobileRequestDTO();
        request.setIndirizzo("Corso Test 10");
        request.setCitta("Milano");
        request.setSuperficie(150.0);
        request.setTipo(TipoImmobile.NEGOZIO);
        request.setVetrine(3);
        request.setMagazzinoMq(40.0);

        mockMvc.perform(post("/api/immobili")
                .with(user(managerUser.getEmail()).roles("MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("NEGOZIO"));
    }

    @Test
    void testCreateUfficio_Success() throws Exception {
        ImmobileRequestDTO request = new ImmobileRequestDTO();
        request.setIndirizzo("Piazza Test 1");
        request.setCitta("Bergamo");
        request.setSuperficie(250.0);
        request.setTipo(TipoImmobile.UFFICIO);
        request.setPostiLavoro(25);
        request.setSaleRiunioni(4);

        mockMvc.perform(post("/api/immobili")
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("UFFICIO"));
    }

    @Test
    void testCreateImmobile_ForbiddenForLocatario() throws Exception {
        ImmobileRequestDTO request = new ImmobileRequestDTO();
        request.setIndirizzo("Via Test 3");
        request.setCitta("Milano");
        request.setSuperficie(100.0);
        request.setTipo(TipoImmobile.APPARTAMENTO);

        mockMvc.perform(post("/api/immobili")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateImmobile_Success() throws Exception {
        ImmobileRequestDTO request = new ImmobileRequestDTO();
        request.setIndirizzo("Via Updated 1");
        request.setCitta("Milano");
        request.setSuperficie(120.0);
        request.setTipo(TipoImmobile.APPARTAMENTO);

        mockMvc.perform(put("/api/immobili/" + testImmobile.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indirizzo").value("Via Updated 1"))
                .andExpect(jsonPath("$.superficie").value(120.0));
    }

    @Test
    void testDeleteImmobile_AdminOnly() throws Exception {
        Immobile toDelete = new Immobile();
        toDelete.setIndirizzo("Via Delete 1");
        toDelete.setCitta("Milano");
        toDelete.setSuperficie(50.0);
        toDelete.setTipo(TipoImmobile.APPARTAMENTO);
        toDelete = immobileRepository.save(toDelete);

        mockMvc.perform(delete("/api/immobili/" + toDelete.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteImmobile_ForbiddenForManager() throws Exception {
        mockMvc.perform(delete("/api/immobili/" + testImmobile.getId())
                .with(user(managerUser.getEmail()).roles("MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetImmobiliPerCitta_Success() throws Exception {
        mockMvc.perform(get("/api/immobili/per-citta")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    void testGetImmobiliPerTipo_Success() throws Exception {
        mockMvc.perform(get("/api/immobili/per-tipo")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }
}

