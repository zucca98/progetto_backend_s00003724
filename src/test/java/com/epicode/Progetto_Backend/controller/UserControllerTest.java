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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.epicode.Progetto_Backend.dto.UserUpdateDTO;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;
import com.epicode.Progetto_Backend.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UserControllerTest - Test unitari per il controller di gestione utenti.
 * 
 * Questa classe testa gli endpoint REST del UserController, verificando:
 * - Recupero profilo utente corrente (/api/users/me)
 * - Aggiornamento profilo utente corrente
 * - Gestione accesso basato su ruoli (solo ADMIN può vedere tutti gli utenti)
 * - Assegnazione ruoli utente (solo ADMIN)
 * - Recupero ruoli disponibili (solo ADMIN)
 * - Gestione autorizzazioni e accesso negato
 * 
 * I test utilizzano MockMvc con Spring Security per simulare richieste
 * HTTP autenticate con diversi ruoli e verificare che gli endpoint
 * rispettino le autorizzazioni configurate.
 * 
 * @see com.epicode.Progetto_Backend.controller.UserController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused"})
class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @SuppressWarnings("unused")
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User adminUser;
    private User regularUser;

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

        // Crea regular user
        regularUser = User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Regular")
                .cognome("User")
                .enabled(true)
                .roles(new HashSet<>(Set.of(locatarioRole)))
                .build();
        regularUser = userRepository.save(regularUser);
    }

    @Test
    void testGetMyProfile_Success() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .with(user(regularUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.nome").value("Regular"));
    }

    @Test
    void testGetMyProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateMyProfile_Success() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setNome("Updated");
        updateDTO.setCognome("Name");

        mockMvc.perform(put("/api/users/me")
                .with(user(regularUser.getEmail()).roles("LOCATARIO"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Updated"))
                .andExpect(jsonPath("$.cognome").value("Name"));
    }

    @Test
    void testGetAllUsers_AdminOnly() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAllUsers_ForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(user(regularUser.getEmail()).roles("LOCATARIO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserById_AdminOnly() throws Exception {
        mockMvc.perform(get("/api/users/" + regularUser.getId())
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(regularUser.getId()))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void testUpdateUserRoles_AdminOnly() throws Exception {
        Set<String> newRoles = Set.of("ROLE_MANAGER");

        mockMvc.perform(put("/api/users/" + regularUser.getId() + "/roles")
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRoles)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateUserRoles_ForbiddenForNonAdmin() throws Exception {
        Set<String> newRoles = Set.of("ROLE_MANAGER");

        mockMvc.perform(put("/api/users/" + regularUser.getId() + "/roles")
                .with(user(regularUser.getEmail()).roles("LOCATARIO"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRoles)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllRoles_AdminOnly() throws Exception {
        mockMvc.perform(get("/api/users/roles")
                .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

