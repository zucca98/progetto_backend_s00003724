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

import com.epicode.Progetto_Backend.dto.LocatarioRequestDTO;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * LocatarioControllerTest - Test unitari per il controller di gestione locatari.
 * 
 * Questa classe testa gli endpoint REST del LocatarioController, verificando:
 * - Creazione locatari
 * - Recupero lista locatari con paginazione
 * - Recupero locatario per ID
 * - Aggiornamento locatari
 * - Eliminazione locatari
 * - Recupero profilo locatario corrente (/api/locatari/me per LOCATARIO)
 * - Query per locatari con contratti di lunga durata
 * - Gestione accesso basato su ruoli (ADMIN, MANAGER possono gestire tutti i locatari)
 * 
 * I test verificano anche la corretta associazione tra Locatario e User
 * e i controlli di accesso appropriati.
 * 
 * @see com.epicode.Progetto_Backend.controller.LocatarioController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused"})
class LocatarioControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocatarioRepository locatarioRepository;

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
        testLocatario = Locatario.builder()
                .nome("Test")
                .cognome("Locatario")
                .cf("TSTLCT80A01H501X")
                .indirizzo("Via Test 1")
                .telefono("1234567890")
                .user(locatarioUser)
                .build();
        testLocatario = locatarioRepository.save(testLocatario);
    }

    @Test
    void testGetAllLocatari_AdminSuccess() throws Exception {
        mockMvc.perform(get("/api/locatari")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetAllLocatari_ManagerSuccess() throws Exception {
        mockMvc.perform(get("/api/locatari")
                .with(user(managerUser.getEmail()).roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetAllLocatari_ForbiddenForLocatario() throws Exception {
        mockMvc.perform(get("/api/locatari")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetLocatarioById_Success() throws Exception {
        mockMvc.perform(get("/api/locatari/" + testLocatario.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLocatario.getId()))
                .andExpect(jsonPath("$.nome").value("Test"));
    }

    @Test
    void testGetMyLocatario_Success() throws Exception {
        mockMvc.perform(get("/api/locatari/me")
                .with(user(locatarioUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Test"));
    }

    @Test
    void testCreateLocatario_Success() throws Exception {
        LocatarioRequestDTO request = new LocatarioRequestDTO();
        request.setNome("New");
        request.setCognome("Locatario");
        request.setCf("NWLCT90B01H501Y");
        request.setIndirizzo("Via New 2");
        request.setTelefono("0987654321");
        request.setUserId(adminUser.getId());

        mockMvc.perform(post("/api/locatari")
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("New"));
    }

    @Test
    void testUpdateLocatario_Success() throws Exception {
        LocatarioRequestDTO request = new LocatarioRequestDTO();
        request.setNome("Updated");
        request.setCognome("Locatario");
        request.setCf("TSTLCT80A01H501X");
        request.setIndirizzo("Via Updated 1");
        request.setTelefono("1111111111");
        request.setUserId(locatarioUser.getId());

        mockMvc.perform(put("/api/locatari/" + testLocatario.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Updated"));
    }

    @Test
    void testDeleteLocatario_AdminOnly() throws Exception {
        Locatario toDelete = Locatario.builder()
                .nome("ToDelete")
                .cognome("Test")
                .cf("TDLTST80A01H501Z")
                .indirizzo("Via Delete 1")
                .telefono("9999999999")
                .user(adminUser)
                .build();
        toDelete = locatarioRepository.save(toDelete);

        mockMvc.perform(delete("/api/locatari/" + toDelete.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteLocatario_ForbiddenForManager() throws Exception {
        mockMvc.perform(delete("/api/locatari/" + testLocatario.getId())
                .with(user(managerUser.getEmail()).roles("MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetLocatariConContrattiLunghi_Success() throws Exception {
        mockMvc.perform(get("/api/locatari/contratti-lunghi")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

