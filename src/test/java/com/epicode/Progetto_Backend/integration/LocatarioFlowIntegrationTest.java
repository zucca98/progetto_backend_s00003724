package com.epicode.Progetto_Backend.integration;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epicode.Progetto_Backend.dto.ContrattoRequestDTO;
import com.epicode.Progetto_Backend.dto.LocatarioRequestDTO;
import com.epicode.Progetto_Backend.entity.FrequenzaRata;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.User;

import jakarta.persistence.EntityManager;

/**
 * LocatarioFlowIntegrationTest - Test di integrazione end-to-end per il flusso completo di gestione locatari.
 * 
 * Questa classe testa l'intero flusso di gestione dei locatari attraverso le chiamate HTTP,
 * verificando l'integrazione tra controller, service, repository e database:
 * - Creazione locatario associato a User
 * - Recupero locatario per ID
 * - Aggiornamento dati locatario
 * - Creazione contratto per locatario
 * - Verifica relazioni tra Locatario, User e Contratto nel database
 * 
 * I test verificano che tutti i componenti dell'applicazione lavorino insieme
 * correttamente e che i dati vengano persistiti e recuperati correttamente
 * attraverso tutti gli strati dell'applicazione.
 * 
 * @see com.epicode.Progetto_Backend.integration.BaseIntegrationTest
 */
@SuppressWarnings({"null", "unused"})
class LocatarioFlowIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private EntityManager entityManager;
    
    @Test
    void testCreateLocatarioWithUserFlow() throws Exception {
        // Setup: crea utente MANAGER
        createTestUser("manager7@test.com", "password123", "ROLE_MANAGER");
        String token = getAuthToken("manager7@test.com", "password123");
        
        // Setup: crea User
        User newUser = createTestUser("newlocatario@test.com", "password123", "ROLE_LOCATARIO");
        
        // 1. Creazione Locatario associato a User
        @SuppressWarnings("null") Long userId = newUser.getId();
        LocatarioRequestDTO locatarioRequest = LocatarioRequestDTO.builder()
                .nome("Mario")
                .cognome("Rossi")
                .cf("RSSMRA80A01H501X")
                .indirizzo("Via Locatario 555")
                .telefono("3331234567")
                .userId(userId)
                .build();
        
        MvcResult result = mockMvc.perform(post("/api/locatari")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locatarioRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Mario"))
                .andExpect(jsonPath("$.cognome").value("Rossi"))
                .andExpect(jsonPath("$.cf").value("RSSMRA80A01H501X"))
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        Locatario createdLocatario = objectMapper.readValue(response, Locatario.class);
        
        // 2. Verifica relazione OneToOne nel database
        Locatario locatario = locatarioRepository.findById(createdLocatario.getId()).orElseThrow();
        assertNotNull(locatario.getUser());
        assertEquals(newUser.getId(), locatario.getUser().getId());
        assertEquals("newlocatario@test.com", locatario.getUser().getEmail());
    }
    
    @Test
    void testLocatarioContractsAccessFlow() throws Exception {
        // Setup: crea utente MANAGER e due locatari
        createTestUser("manager8@test.com", "password123", "ROLE_MANAGER");
        String managerToken = getAuthToken("manager8@test.com", "password123");
        
        User locatario1User = createTestUser("locatario5@test.com", "password123", "ROLE_LOCATARIO");
        String locatario1Token = getAuthToken("locatario5@test.com", "password123");
        Locatario locatario1 = createTestLocatario(locatario1User, "TESTCF33333333");
        
        User locatario2User = createTestUser("locatario6@test.com", "password123", "ROLE_LOCATARIO");
        Locatario locatario2 = createTestLocatario(locatario2User, "TESTCF44444444");
        
        Immobile immobile1 = createTestImmobile("Via Locatario1 666", "Torino");
        Immobile immobile2 = createTestImmobile("Via Locatario2 777", "Palermo");
        
        // 1. Crea contratto per locatario1
        @SuppressWarnings("null") Long locatario1Id = locatario1.getId();
        @SuppressWarnings("null") Long immobile1Id = immobile1.getId();
        ContrattoRequestDTO contrattoRequest1 = ContrattoRequestDTO.builder()
                .locatarioId(locatario1Id)
                .immobileId(immobile1Id)
                .dataInizio(LocalDate.now())
                .durataAnni(1)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.TRIMESTRALE)
                .build();
        
        mockMvc.perform(post("/api/contratti")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contrattoRequest1)))
                .andExpect(status().isCreated());
        
        // 2. Crea contratto per locatario2
        @SuppressWarnings("null") Long locatario2Id = locatario2.getId();
        @SuppressWarnings("null") Long immobile2Id = immobile2.getId();
        ContrattoRequestDTO contrattoRequest2 = ContrattoRequestDTO.builder()
                .locatarioId(locatario2Id)
                .immobileId(immobile2Id)
                .dataInizio(LocalDate.now())
                .durataAnni(1)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.TRIMESTRALE)
                .build();
        
        mockMvc.perform(post("/api/contratti")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contrattoRequest2)))
                .andExpect(status().isCreated());
        
        // 3. Login come LOCATARIO1 e verifica accesso solo ai propri contratti
        mockMvc.perform(get("/api/contratti/me")
                .header("Authorization", "Bearer " + locatario1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].locatario.id").value(locatario1Id));
        
        // 4. Flush e clear per forzare il caricamento dal database
        entityManager.flush();
        entityManager.clear();
        
        // 5. Verifica nel database
        Locatario locatario1WithContracts = locatarioRepository.findById(locatario1Id).orElseThrow();
        assertEquals(1, locatario1WithContracts.getContratti().size());
    }
    
    @Test
    void testLocatarioManutenzioniFlow() throws Exception {
        // Setup: crea utente MANAGER, locatario e immobile
        createTestUser("manager9@test.com", "password123", "ROLE_MANAGER");
        String token = getAuthToken("manager9@test.com", "password123");
        
        User locatarioUser = createTestUser("locatario7@test.com", "password123", "ROLE_LOCATARIO");
        Locatario locatario = createTestLocatario(locatarioUser, "TESTCF55555555");
        
        Immobile immobile = createTestImmobile("Via Manutenzione 888", "Bari");
        
        // 1. Crea manutenzione associata a locatario e immobile
        @SuppressWarnings("null") Long immobileId = immobile.getId();
        @SuppressWarnings("null") Long locatarioId = locatario.getId();
        com.epicode.Progetto_Backend.dto.ManutenzioneRequestDTO manutenzioneRequest = 
                com.epicode.Progetto_Backend.dto.ManutenzioneRequestDTO.builder()
                .immobileId(immobileId)
                .locatarioId(locatarioId)
                .descrizione("Riparazione caldaia")
                .importo(500.0)
                .dataMan(LocalDate.now())
                .build();
        
        mockMvc.perform(post("/api/manutenzioni")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(manutenzioneRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.locatario.id").value(locatarioId))
                .andExpect(jsonPath("$.immobile.id").value(immobileId));
        
        // 2. Flush e clear per forzare il caricamento dal database
        entityManager.flush();
        entityManager.clear();
        
        // 3. Verifica associazione nel database
        Locatario locatarioWithManutenzioni = locatarioRepository.findById(locatarioId).orElseThrow();
        assertNotNull(locatarioWithManutenzioni.getManutenzioni());
        assertEquals(1, locatarioWithManutenzioni.getManutenzioni().size());
        
        // 3. Verifica filtri per ruolo LOCATARIO
        String locatarioToken = getAuthToken("locatario7@test.com", "password123");
        mockMvc.perform(get("/api/manutenzioni/me")
                .header("Authorization", "Bearer " + locatarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        
        // 4. Verifica filtri per ruolo LOCATARIO (già fatto sopra)
    }
}

