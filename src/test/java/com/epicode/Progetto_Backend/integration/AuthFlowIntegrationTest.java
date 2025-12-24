package com.epicode.Progetto_Backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epicode.Progetto_Backend.dto.LoginRequestDTO;
import com.epicode.Progetto_Backend.dto.RegisterRequestDTO;
import com.epicode.Progetto_Backend.dto.UserUpdateDTO;
import com.epicode.Progetto_Backend.entity.User;

/**
 * AuthFlowIntegrationTest - Test di integrazione end-to-end per il flusso completo di autenticazione e gestione utente.
 * 
 * Questa classe testa l'intero flusso di autenticazione e gestione utente attraverso
 * le chiamate HTTP reali, verificando l'integrazione tra tutti i componenti:
 * - Registrazione nuovi utenti con generazione token JWT
 * - Login utenti con credenziali valide
 * - Accesso a endpoint protetti con token JWT
 * - Aggiornamento profilo utente corrente
 * - Upload immagine profilo
 * - Assegnazione ruoli utente (solo ADMIN)
 * - Verifica persistenza dei dati nel database
 * 
 * I test verificano che l'intero flusso di autenticazione funzioni correttamente
 * end-to-end, dalla registrazione all'utilizzo del token per accedere agli endpoint
 * protetti, garantendo che Spring Security, JWT, e i servizi di autenticazione
 * lavorino insieme correttamente.
 * 
 * @see com.epicode.Progetto_Backend.integration.BaseIntegrationTest
 */
@SuppressWarnings({"null", "unused"})
class AuthFlowIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void testCompleteUserRegistrationAndLoginFlow() throws Exception {
        // 1. Registrazione
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("newuser@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setNome("New");
        registerRequest.setCognome("User");
        
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.nome").value("New"))
                .andExpect(jsonPath("$.cognome").value("User"))
                .andReturn();
        
        String token = extractToken(registerResult);
        assertNotNull(token);
        
        // 2. Login con credenziali appena create
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("newuser@test.com");
        loginRequest.setPassword("password123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("newuser@test.com"));
        
        // 3. Accesso a endpoint protetto
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.nome").value("New"))
                .andExpect(jsonPath("$.cognome").value("User"));
        
        // 4. Verifica nel database
        User savedUser = userRepository.findByEmail("newuser@test.com").orElseThrow();
        assertNotNull(savedUser);
        assertEquals("New", savedUser.getNome());
        assertEquals("User", savedUser.getCognome());
    }
    
    @Test
    void testUserProfileUpdateFlow() throws Exception {
        // Setup: crea utente e ottieni token
        createTestUser("updateuser@test.com", "password123");
        String token = getAuthToken("updateuser@test.com", "password123");
        
        // 1. Aggiorna profilo
        UserUpdateDTO updateRequest = new UserUpdateDTO();
        updateRequest.setNome("Updated");
        updateRequest.setCognome("Name");
        
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Updated"))
                .andExpect(jsonPath("$.cognome").value("Name"));
        
        // 2. Verifica modifica nel database
        User updatedUser = userRepository.findByEmail("updateuser@test.com").orElseThrow();
        assertEquals("Updated", updatedUser.getNome());
        assertEquals("Name", updatedUser.getCognome());
    }
    
    @Test
    void testUserProfileImageUploadFlow() throws Exception {
        // Setup: crea utente e ottieni token
        createTestUser("imageuser@test.com", "password123");
        String token = getAuthToken("imageuser@test.com", "password123");
        
        // 1. Aggiorna immagine profilo tramite URL
        UserUpdateDTO updateRequest = new UserUpdateDTO();
        updateRequest.setProfileImage("https://example.com/profile.jpg");
        
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImage").value("https://example.com/profile.jpg"));
        
        // 2. Verifica URL aggiornato nel database
        User updatedUser = userRepository.findByEmail("imageuser@test.com").orElseThrow();
        assertEquals("https://example.com/profile.jpg", updatedUser.getProfileImage());
    }
    
    @Test
    void testUserRoleAssignmentFlow() throws Exception {
        // Setup: crea ruolo ADMIN se non esiste
        roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    com.epicode.Progetto_Backend.entity.Role role = com.epicode.Progetto_Backend.entity.Role.builder()
                            .name("ROLE_ADMIN")
                            .build();
                    return roleRepository.save(role);
                });
        
        // Setup: crea utente ADMIN e ottieni token
        createTestUser("admin@test.com", "password123", "ROLE_ADMIN");
        String adminToken = getAuthToken("admin@test.com", "password123");
        
        // Setup: crea utente normale
        User normalUser = createTestUser("normal@test.com", "password123", "ROLE_LOCATARIO");
        
        // 1. Assegna ruolo MANAGER all'utente normale
        java.util.Set<String> roles = new java.util.HashSet<>();
        roles.add("ROLE_MANAGER");
        
        roleRepository.findByName("ROLE_MANAGER")
                .orElseGet(() -> {
                    com.epicode.Progetto_Backend.entity.Role role = com.epicode.Progetto_Backend.entity.Role.builder()
                            .name("ROLE_MANAGER")
                            .build();
                    return roleRepository.save(role);
                });
        
        mockMvc.perform(put("/api/users/" + normalUser.getId() + "/roles")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roles)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray());
        
        // 2. Verifica permessi nel database
        User updatedUser = userRepository.findByEmail("normal@test.com").orElseThrow();
        assertNotNull(updatedUser.getRoles());
        assertEquals(1, updatedUser.getRoles().size());
        assertEquals("ROLE_MANAGER", updatedUser.getRoles().iterator().next().getName());
    }
}

