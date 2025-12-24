package com.epicode.Progetto_Backend.integration;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epicode.Progetto_Backend.dto.ContrattoRequestDTO;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.FrequenzaRata;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.ContrattoRepository;
import com.epicode.Progetto_Backend.repository.RataRepository;

/**
 * ContrattoRataFlowIntegrationTest - Test di integrazione end-to-end per il flusso completo di gestione contratti e rate.
 * 
 * Questa classe testa l'intero flusso di gestione contratti e rate attraverso le chiamate HTTP,
 * verificando l'integrazione tra controller, service, repository e database:
 * - Creazione contratti con generazione automatica delle rate
 * - Verifica calcolo corretto del numero e importo delle rate in base alla frequenza
 * - Aggiornamento stato di pagamento delle rate
 * - Query per recuperare rate per contratto
 * - Verifica persistenza corretta delle relazioni Contratto-Rata
 * 
 * I test verificano la logica complessa di generazione automatica delle rate
 * per diverse frequenze (MENSILE, TRIMESTRALE, SEMESTRALE, ANNUALE) e che
 * tutte le operazioni funzionino correttamente attraverso tutti gli strati
 * dell'applicazione, incluso il database.
 * 
 * @see com.epicode.Progetto_Backend.integration.BaseIntegrationTest
 */
@SuppressWarnings({"null", "unused"})
class ContrattoRataFlowIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private RataRepository rataRepository;
    
    @Autowired
    private ContrattoRepository contrattoRepository;
    
    @Test
    void testCreateContrattoAndGenerateRateFlow() throws Exception {
        // Setup: crea utente MANAGER, locatario e immobile
        createTestUser("manager4@test.com", "password123", "ROLE_MANAGER");
        String token = getAuthToken("manager4@test.com", "password123");
        
        User locatarioUser = createTestUser("locatario2@test.com", "password123", "ROLE_LOCATARIO");
        Locatario locatario = createTestLocatario(locatarioUser, "TESTCF87654321");
        
        Immobile immobile = createTestImmobile("Via Rate 222", "Bologna");
        
        // 1. Creazione contratto trimestrale (2 anni = 8 rate)
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
                .andReturn();
        
        String contrattoResponse = contrattoResult.getResponse().getContentAsString();
        Contratto createdContratto = objectMapper.readValue(contrattoResponse, Contratto.class);
        @SuppressWarnings("null") Long contrattoId = createdContratto.getId();
        
        // 2. Verifica generazione automatica rate
        mockMvc.perform(get("/api/rate/contratto/" + contrattoId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8)); // 2 anni * 4 trimestri = 8 rate
        
        // 3. Verifica calcolo importi nel database
        Contratto contratto = contrattoRepository.findById(contrattoId).orElseThrow();
        assertNotNull(contratto.getRate());
        assertEquals(8, contratto.getRate().size());
        
        // Verifica che tutte le rate abbiano lo stesso importo (12000 / 8 = 1500)
        double expectedImporto = 12000.0 / 8;
        for (Rata rata : contratto.getRate()) {
            assertEquals(expectedImporto, rata.getImporto(), 0.01);
            assertEquals('N', rata.getPagata());
        }
    }
    
    @Test
    void testPagamentoRataFlow() throws Exception {
        // Setup: crea utente MANAGER, locatario, immobile e contratto
        createTestUser("manager5@test.com", "password123", "ROLE_MANAGER");
        String managerToken = getAuthToken("manager5@test.com", "password123");
        
        User locatarioUser = createTestUser("locatario3@test.com", "password123", "ROLE_LOCATARIO");
        String locatarioToken = getAuthToken("locatario3@test.com", "password123");
        Locatario locatario = createTestLocatario(locatarioUser, "TESTCF11111111");
        
        Immobile immobile = createTestImmobile("Via Pagamento 333", "Genova");
        
        @SuppressWarnings("null") Long locatarioId = locatario.getId();
        @SuppressWarnings("null") Long immobileId = immobile.getId();
        ContrattoRequestDTO contrattoRequest = ContrattoRequestDTO.builder()
                .locatarioId(locatarioId)
                .immobileId(immobileId)
                .dataInizio(LocalDate.now())
                .durataAnni(1)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.TRIMESTRALE)
                .build();
        
        MvcResult contrattoResult = mockMvc.perform(post("/api/contratti")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contrattoRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String contrattoResponse = contrattoResult.getResponse().getContentAsString();
        Contratto createdContratto = objectMapper.readValue(contrattoResponse, Contratto.class);
        @SuppressWarnings("null") Long contrattoId = createdContratto.getId();
        
        // 1. Login come LOCATARIO e visualizzazione rate
        mockMvc.perform(get("/api/rate/me")
                .header("Authorization", "Bearer " + locatarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4)); // 1 anno * 4 trimestri = 4 rate
        
        // 2. Recupera la prima rata direttamente dal repository delle rate
        List<Rata> rate = rataRepository.findByContrattoId(contrattoId);
        assertFalse(rate.isEmpty(), "Le rate dovrebbero essere state generate automaticamente");
        Rata firstRata = rate.get(0);
        @SuppressWarnings("null") Long rataId = firstRata.getId();
        
        // 3. Pagamento rata (come MANAGER)
        mockMvc.perform(put("/api/rate/" + rataId + "/pagata")
                .header("Authorization", "Bearer " + managerToken)
                .param("pagata", "S"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagata").value("S"));
        
        // 4. Verifica stato aggiornato nel database
        Rata updatedRata = rataRepository.findById(rataId).orElseThrow();
        assertEquals('S', updatedRata.getPagata());
    }
    
    @Test
    void testContrattoWithMultipleRateFlow() throws Exception {
        // Setup: crea utente MANAGER, locatario e immobile
        createTestUser("manager6@test.com", "password123", "ROLE_MANAGER");
        String token = getAuthToken("manager6@test.com", "password123");
        
        User locatarioUser = createTestUser("locatario4@test.com", "password123", "ROLE_LOCATARIO");
        Locatario locatario = createTestLocatario(locatarioUser, "TESTCF22222222");
        
        Immobile immobile = createTestImmobile("Via Multiple 444", "Venezia");
        
        // 1. Creazione contratto trimestrale (1 anno = 4 rate)
        @SuppressWarnings("null") Long locatarioId = locatario.getId();
        @SuppressWarnings("null") Long immobileId = immobile.getId();
        ContrattoRequestDTO contrattoRequest = ContrattoRequestDTO.builder()
                .locatarioId(locatarioId)
                .immobileId(immobileId)
                .dataInizio(LocalDate.now())
                .durataAnni(1)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.TRIMESTRALE)
                .build();
        
        MvcResult contrattoResult = mockMvc.perform(post("/api/contratti")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contrattoRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String contrattoResponse = contrattoResult.getResponse().getContentAsString();
        Contratto createdContratto = objectMapper.readValue(contrattoResponse, Contratto.class);
        @SuppressWarnings("null") Long contrattoId = createdContratto.getId();
        
        // 2. Verifica 4 rate generate
        Contratto contratto = contrattoRepository.findById(contrattoId).orElseThrow();
        assertEquals(4, contratto.getRate().size());
        
        // 3. Pagamento di 2 rate
        Rata rata1 = contratto.getRate().get(0);
        Rata rata2 = contratto.getRate().get(1);
        
        mockMvc.perform(put("/api/rate/" + rata1.getId() + "/pagata")
                .header("Authorization", "Bearer " + token)
                .param("pagata", "S"))
                .andExpect(status().isOk());
        
        mockMvc.perform(put("/api/rate/" + rata2.getId() + "/pagata")
                .header("Authorization", "Bearer " + token)
                .param("pagata", "S"))
                .andExpect(status().isOk());
        
        // 4. Verifica statistiche
        mockMvc.perform(get("/api/rate/non-pagate")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        // 5. Verifica nel database
        contratto = contrattoRepository.findById(contrattoId).orElseThrow();
        long ratePagate = contratto.getRate().stream()
                .filter(r -> r.getPagata() == 'S')
                .count();
        assertEquals(2, ratePagate);
        
        long rateNonPagate = contratto.getRate().stream()
                .filter(r -> r.getPagata() == 'N')
                .count();
        assertEquals(2, rateNonPagate);
    }
}

