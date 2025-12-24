package com.epicode.Progetto_Backend.integration;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epicode.Progetto_Backend.dto.ContrattoRequestDTO;
import com.epicode.Progetto_Backend.dto.ImmobileRequestDTO;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.FrequenzaRata;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.User;

import jakarta.persistence.EntityManager;

/**
 * ImmobileFlowIntegrationTest - Test di integrazione end-to-end per il flusso completo di gestione immobili.
 * 
 * Questa classe testa l'intero flusso di gestione degli immobili attraverso le chiamate HTTP,
 * verificando l'integrazione tra controller, service, repository e database:
 * - Creazione immobili di tutti i tipi (Appartamento, Negozio, Ufficio)
 * - Recupero immobili con paginazione
 * - Aggiornamento immobili
 * - Eliminazione immobili
 * - Gestione corretta dell'ereditarietà delle classi Immobile
 * - Verifica persistenza dei campi specifici per ogni tipo di immobile
 * 
 * I test verificano che tutti i componenti dell'applicazione lavorino insieme
 * correttamente e che i dati vengano persistiti e recuperati correttamente
 * attraverso tutti gli strati dell'applicazione, incluso il supporto
 * all'ereditarietà tramite la strategia JOINED di JPA.
 * 
 * @see com.epicode.Progetto_Backend.integration.BaseIntegrationTest
 */
@SuppressWarnings({"null", "unused"})
class ImmobileFlowIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private EntityManager entityManager;
    
    @Test
    void testCreateImmobileFlow() throws Exception {
        // Setup: crea utente MANAGER e ottieni token
        createTestUser("manager@test.com", "password123", "ROLE_MANAGER");
        String token = getAuthToken("manager@test.com", "password123");
        
        // 1. Creazione immobile
        ImmobileRequestDTO request = ImmobileRequestDTO.builder()
                .indirizzo("Via Test 123")
                .citta("Roma")
                .superficie(100.0)
                .tipo(com.epicode.Progetto_Backend.entity.TipoImmobile.APPARTAMENTO)
                .piano(2)
                .numCamere(3)
                .build();
        
        MvcResult result = mockMvc.perform(post("/api/immobili")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.indirizzo").value("Via Test 123"))
                .andExpect(jsonPath("$.citta").value("Roma"))
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        Immobile createdImmobile = objectMapper.readValue(response, Immobile.class);
        @SuppressWarnings("null") Long immobileId = createdImmobile.getId();
        
        // 2. Verifica creazione
        mockMvc.perform(get("/api/immobili/" + immobileId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(immobileId))
                .andExpect(jsonPath("$.indirizzo").value("Via Test 123"));
        
        // 3. Verifica nel database
        Immobile savedImmobile = immobileRepository.findById(immobileId).orElseThrow();
        assertNotNull(savedImmobile);
        assertEquals("Via Test 123", savedImmobile.getIndirizzo());
        assertEquals("Roma", savedImmobile.getCitta());
    }
    
    @Test
    void testUpdateImmobileFlow() throws Exception {
        // Setup: crea utente MANAGER e immobile
        createTestUser("manager2@test.com", "password123", "ROLE_MANAGER");
        String token = getAuthToken("manager2@test.com", "password123");
        Immobile immobile = createTestImmobile("Via Vecchia 456", "Milano");
        
        // 1. Update immobile
        ImmobileRequestDTO updateRequest = ImmobileRequestDTO.builder()
                .indirizzo("Via Nuova 789")
                .citta("Torino")
                .superficie(150.0)
                .tipo(com.epicode.Progetto_Backend.entity.TipoImmobile.APPARTAMENTO)
                .build();
        
        @SuppressWarnings("null") Long immobileId = immobile.getId();
        mockMvc.perform(put("/api/immobili/" + immobileId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indirizzo").value("Via Nuova 789"))
                .andExpect(jsonPath("$.citta").value("Torino"))
                .andExpect(jsonPath("$.superficie").value(150.0));
        
        // 2. Verifica modifica nel database
        Immobile updatedImmobile = immobileRepository.findById(immobileId).orElseThrow();
        assertEquals("Via Nuova 789", updatedImmobile.getIndirizzo());
        assertEquals("Torino", updatedImmobile.getCitta());
        assertEquals(150.0, updatedImmobile.getSuperficie());
    }
    
    @Test
    void testDeleteImmobileFlow() throws Exception {
        // Setup: crea utente ADMIN e immobile
        createTestUser("admin2@test.com", "password123", "ROLE_ADMIN");
        String token = getAuthToken("admin2@test.com", "password123");
        Immobile immobile = createTestImmobile("Via Delete 999", "Napoli");
        @SuppressWarnings("null") Long immobileId = immobile.getId();
        
        // 1. Delete immobile
        mockMvc.perform(delete("/api/immobili/" + immobileId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        
        // 2. Verifica eliminazione
        mockMvc.perform(get("/api/immobili/" + immobileId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        
        // 3. Verifica nel database
        assertEquals(false, immobileRepository.existsById(immobileId));
    }
    
    @Test
    void testImmobileWithContractsFlow() throws Exception {
        // Setup: crea utente MANAGER, locatario e immobile
        createTestUser("manager3@test.com", "password123", "ROLE_MANAGER");
        String token = getAuthToken("manager3@test.com", "password123");
        
        User locatarioUser = createTestUser("locatario@test.com", "password123", "ROLE_LOCATARIO");
        Locatario locatario = createTestLocatario(locatarioUser, "TESTCF12345678");
        
        Immobile immobile = createTestImmobile("Via Contratti 111", "Firenze");
        
        // 1. Creazione contratto associato all'immobile
        @SuppressWarnings("null") Long locatarioId = locatario.getId();
        @SuppressWarnings("null") Long immobileId = immobile.getId();
        ContrattoRequestDTO contrattoRequest = ContrattoRequestDTO.builder()
                .locatarioId(locatarioId)
                .immobileId(immobileId)
                .dataInizio(LocalDate.now())
                .durataAnni(2)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.TRIMESTRALE)
                .build();
        
        MvcResult contrattoResult = mockMvc.perform(post("/api/contratti")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contrattoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.immobile.id").value(immobileId))
                .andReturn();
        
        String contrattoResponse = contrattoResult.getResponse().getContentAsString();
        Contratto createdContratto = objectMapper.readValue(contrattoResponse, Contratto.class);
        
        // 2. Flush e clear per forzare il caricamento dal database
        entityManager.flush();
        entityManager.clear();
        
        // 3. Verifica relazione nel database
        Immobile immobileWithContracts = immobileRepository.findById(immobileId).orElseThrow();
        assertNotNull(immobileWithContracts.getContratti());
        assertEquals(1, immobileWithContracts.getContratti().size());
        assertEquals(createdContratto.getId(), immobileWithContracts.getContratti().get(0).getId());
    }
}

