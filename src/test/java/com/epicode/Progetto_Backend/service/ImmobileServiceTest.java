package com.epicode.Progetto_Backend.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.ImmobileRequestDTO;
import com.epicode.Progetto_Backend.entity.Appartamento;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Negozio;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.Ufficio;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;

/**
 * ImmobileServiceTest - Test unitari per il servizio di gestione immobili.
 * 
 * Questa classe testa i metodi del ImmobileService, verificando:
 * - Creazione immobili di tutti i tipi (Appartamento, Negozio, Ufficio)
 * - Recupero lista immobili con paginazione
 * - Recupero immobile per ID
 * - Aggiornamento immobili (con gestione corretta dei tipi ereditari)
 * - Eliminazione immobili
 * - Statistiche immobili
 * - Gestione degli errori (immobile non trovato)
 * 
 * I test verificano la corretta gestione dell'ereditarietà delle classi Immobile
 * e la persistenza dei campi specifici di ogni sottotipo.
 * 
 * @see com.epicode.Progetto_Backend.service.ImmobileService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class ImmobileServiceTest {

    @Autowired
    private ImmobileService immobileService;

    @Autowired
    private ImmobileRepository immobileRepository;

    @Test
    void testGetAllImmobili() {
        Pageable pageable = PageRequest.of(0, 100);
        Page<Immobile> immobili = immobileService.getAllImmobili(pageable);
        assertNotNull(immobili);
        assertNotNull(immobili.getContent());
    }

    @Test
    void testGetImmobileById() {
        ImmobileRequestDTO request = ImmobileRequestDTO.builder()
                .indirizzo("Via Test 1")
                .citta("Roma")
                .superficie(80.0)
                .tipo(TipoImmobile.APPARTAMENTO)
                .piano(2)
                .numCamere(3)
                .build();

        Immobile created = immobileService.createImmobile(request);
        Immobile found = immobileService.getImmobileById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Via Test 1", found.getIndirizzo());
        assertTrue(found instanceof Appartamento);
    }

    @Test
    void testGetImmobileById_NotFound() {
        com.epicode.Progetto_Backend.exception.EntityNotFoundException exception = assertThrows(
                com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> immobileService.getImmobileById(99999L));
        assertNotNull(exception);
    }

    @Test
    void testCreateImmobile_Appartamento() {
        ImmobileRequestDTO request = ImmobileRequestDTO.builder()
                .indirizzo("Via Appartamento 1")
                .citta("Milano")
                .superficie(90.0)
                .tipo(TipoImmobile.APPARTAMENTO)
                .piano(3)
                .numCamere(4)
                .build();

        Immobile immobile = immobileService.createImmobile(request);

        assertNotNull(immobile);
        assertNotNull(immobile.getId());
        assertEquals(TipoImmobile.APPARTAMENTO, immobile.getTipo());
        assertTrue(immobile instanceof Appartamento);
        Appartamento app = (Appartamento) immobile;
        assertEquals(3, app.getPiano());
        assertEquals(4, app.getNumCamere());
    }

    @Test
    void testCreateImmobile_Ufficio() {
        ImmobileRequestDTO request = ImmobileRequestDTO.builder()
                .indirizzo("Via Ufficio 1")
                .citta("Torino")
                .superficie(150.0)
                .tipo(TipoImmobile.UFFICIO)
                .postiLavoro(10)
                .saleRiunioni(2)
                .build();

        Immobile immobile = immobileService.createImmobile(request);

        assertNotNull(immobile);
        assertEquals(TipoImmobile.UFFICIO, immobile.getTipo());
        assertTrue(immobile instanceof Ufficio);
        Ufficio uff = (Ufficio) immobile;
        assertEquals(10, uff.getPostiLavoro());
        assertEquals(2, uff.getSaleRiunioni());
    }

    @Test
    void testCreateImmobile_Negozio() {
        ImmobileRequestDTO request = ImmobileRequestDTO.builder()
                .indirizzo("Via Negozio 1")
                .citta("Napoli")
                .superficie(100.0)
                .tipo(TipoImmobile.NEGOZIO)
                .vetrine(3)
                .magazzinoMq(50.0)
                .build();

        Immobile immobile = immobileService.createImmobile(request);

        assertNotNull(immobile);
        assertEquals(TipoImmobile.NEGOZIO, immobile.getTipo());
        assertTrue(immobile instanceof Negozio);
        Negozio neg = (Negozio) immobile;
        assertEquals(3, neg.getVetrine());
        assertEquals(50.0, neg.getMagazzinoMq());
    }

    @Test
    void testUpdateImmobile() {
        ImmobileRequestDTO createRequest = ImmobileRequestDTO.builder()
                .indirizzo("Via Test 1")
                .citta("Roma")
                .superficie(80.0)
                .tipo(TipoImmobile.APPARTAMENTO)
                .piano(2)
                .numCamere(3)
                .build();

        Immobile immobile = immobileService.createImmobile(createRequest);

        ImmobileRequestDTO updateRequest = ImmobileRequestDTO.builder()
                .indirizzo("Via Aggiornata 1")
                .superficie(100.0)
                .numCamere(4)
                .build();

        Immobile updated = immobileService.updateImmobile(immobile.getId(), updateRequest);

        assertEquals("Via Aggiornata 1", updated.getIndirizzo());
        assertEquals(100.0, updated.getSuperficie());
        assertTrue(updated instanceof Appartamento);
        Appartamento app = (Appartamento) updated;
        assertEquals(4, app.getNumCamere());
    }

    @Test
    void testDeleteImmobile() {
        ImmobileRequestDTO request = ImmobileRequestDTO.builder()
                .indirizzo("Via Test 1")
                .citta("Roma")
                .superficie(80.0)
                .tipo(TipoImmobile.APPARTAMENTO)
                .piano(2)
                .numCamere(3)
                .build();

        Immobile immobile = immobileService.createImmobile(request);
        Long id = immobile.getId();

        immobileService.deleteImmobile(id);

        assertFalse(immobileRepository.existsById(id));
    }

    @Test
    void testGetImmobiliAffittatiPerCitta() {
        Map<String, Long> result = immobileService.getImmobiliAffittatiPerCitta();
        assertNotNull(result);
    }

    @Test
    void testGetContImmobiliPerTipo() {
        Map<TipoImmobile, Long> result = immobileService.getContImmobiliPerTipo();
        assertNotNull(result);
    }
}

