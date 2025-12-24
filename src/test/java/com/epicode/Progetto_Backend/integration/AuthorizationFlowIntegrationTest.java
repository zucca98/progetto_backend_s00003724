package com.epicode.Progetto_Backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epicode.Progetto_Backend.dto.ImmobileRequestDTO;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.User;

/**
 * AuthorizationFlowIntegrationTest - Test di integrazione end-to-end per il flusso completo di autorizzazione e permessi.
 * 
 * Questa classe testa l'intero sistema di autorizzazione dell'applicazione attraverso
 * chiamate HTTP reali, verificando che i controlli di accesso funzionino correttamente:
 * - Accesso completo ADMIN a tutti gli endpoint
 * - Accesso limitato MANAGER (può creare/modificare ma non eliminare immobili, non può accedere a /api/users)
 * - Accesso ristretto LOCATARIO (può vedere solo i propri dati, non può creare/modificare)
 * - Negazione accesso per utenti non autenticati
 * - Verifica che i controlli di accesso siano applicati correttamente a livello di endpoint
 * 
 * I test verificano che Spring Security e i controlli @PreAuthorize funzionino
 * correttamente in un contesto di integrazione completa, non solo a livello unitario.
 * Questo è importante per garantire che la sicurezza dell'applicazione sia effettiva
 * end-to-end.
 * 
 * @see com.epicode.Progetto_Backend.integration.BaseIntegrationTest
 */
@SuppressWarnings({"null", "unused"})
class AuthorizationFlowIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void testAdminFullAccessFlow() throws Exception {
        // Setup: crea utente ADMIN
        createTestUser("admin3@test.com", "password123", "ROLE_ADMIN");
        String adminToken = getAuthToken("admin3@test.com", "password123");
        
        // 1. Verifica accesso a tutti gli endpoint
        // GET /api/users
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        
        // POST /api/immobili
        ImmobileRequestDTO immobileRequest = ImmobileRequestDTO.builder()
                .indirizzo("Via Admin 999")
                .citta("Roma")
                .superficie(100.0)
                .tipo(com.epicode.Progetto_Backend.entity.TipoImmobile.APPARTAMENTO)
                .piano(1)
                .numCamere(3)
                .build();
        
        mockMvc.perform(post("/api/immobili")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(immobileRequest)))
                .andExpect(status().isCreated());
        
        // DELETE /api/immobili/{id}
        Immobile immobile = createTestImmobile("Via Admin Delete", "Milano");
        @SuppressWarnings("null") Long immobileId = immobile.getId();
        mockMvc.perform(delete("/api/immobili/" + immobileId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void testManagerLimitedAccessFlow() throws Exception {
        // Setup: crea utente MANAGER
        createTestUser("manager10@test.com", "password123", "ROLE_MANAGER");
        String managerToken = getAuthToken("manager10@test.com", "password123");
        
        // 1. Verifica accesso consentito
        // GET /api/immobili
        mockMvc.perform(get("/api/immobili")
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
        
        // POST /api/immobili
        ImmobileRequestDTO immobileRequest = ImmobileRequestDTO.builder()
                .indirizzo("Via Manager 111")
                .citta("Torino")
                .superficie(100.0)
                .tipo(com.epicode.Progetto_Backend.entity.TipoImmobile.APPARTAMENTO)
                .piano(2)
                .numCamere(4)
                .build();
        
        mockMvc.perform(post("/api/immobili")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(immobileRequest)))
                .andExpect(status().isCreated());
        
        // 2. Verifica DELETE negato
        Immobile immobile = createTestImmobile("Via Manager Delete", "Napoli");
        @SuppressWarnings("null") Long immobileId = immobile.getId();
        mockMvc.perform(delete("/api/immobili/" + immobileId)
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
        
        // 3. Verifica accesso negato a /api/users
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testLocatarioRestrictedAccessFlow() throws Exception {
        // Setup: crea utente LOCATARIO e un altro locatario
        User locatario1User = createTestUser("locatario8@test.com", "password123", "ROLE_LOCATARIO");
        String locatario1Token = getAuthToken("locatario8@test.com", "password123");
        createTestLocatario(locatario1User, "TESTCF66666666");
        
        User locatario2User = createTestUser("locatario9@test.com", "password123", "ROLE_LOCATARIO");
        Locatario locatario2 = createTestLocatario(locatario2User, "TESTCF77777777");
        
        // 1. Verifica accesso solo ai propri dati
        // GET /api/contratti/me
        mockMvc.perform(get("/api/contratti/me")
                .header("Authorization", "Bearer " + locatario1Token))
                .andExpect(status().isOk());
        
        // GET /api/locatari/me
        mockMvc.perform(get("/api/locatari/me")
                .header("Authorization", "Bearer " + locatario1Token))
                .andExpect(status().isOk());
        
        // GET /api/rate/me
        mockMvc.perform(get("/api/rate/me")
                .header("Authorization", "Bearer " + locatario1Token))
                .andExpect(status().isOk());
        
        // 2. Verifica accesso negato ad altri dati
        // GET /api/locatari/{id} (altro locatario)
        @SuppressWarnings("null") Long locatario2Id = locatario2.getId();
        mockMvc.perform(get("/api/locatari/" + locatario2Id)
                .header("Authorization", "Bearer " + locatario1Token))
                .andExpect(status().isForbidden());
        
        // GET /api/users
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + locatario1Token))
                .andExpect(status().isForbidden());
        
        // POST /api/immobili
        ImmobileRequestDTO immobileRequest = ImmobileRequestDTO.builder()
                .indirizzo("Via Locatario 222")
                .citta("Firenze")
                .superficie(100.0)
                .tipo(com.epicode.Progetto_Backend.entity.TipoImmobile.APPARTAMENTO)
                .build();
        
        mockMvc.perform(post("/api/immobili")
                .header("Authorization", "Bearer " + locatario1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(immobileRequest)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testUnauthenticatedAccessDenied() throws Exception {
        // Verifica che gli endpoint protetti richiedano autenticazione
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/immobili"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/contratti"))
                .andExpect(status().isUnauthorized());
    }
}

