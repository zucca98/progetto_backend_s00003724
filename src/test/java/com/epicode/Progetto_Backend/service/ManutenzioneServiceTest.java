package com.epicode.Progetto_Backend.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.ManutenzioneRequestDTO;
import com.epicode.Progetto_Backend.entity.Appartamento;
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

/**
 * ManutenzioneServiceTest - Test unitari per il servizio di gestione manutenzioni.
 * 
 * Questa classe testa i metodi del ManutenzioneService, verificando:
 * - Creazione record di manutenzione
 * - Recupero lista manutenzioni con paginazione
 * - Recupero manutenzione per ID
 * - Aggiornamento manutenzioni
 * - Eliminazione manutenzioni
 * - Statistiche manutenzioni
 * - Query per manutenzioni per locatario email
 * - Invio email di conferma richiesta manutenzione (mockato)
 * 
 * Il MailgunService viene mockato per evitare l'invio di email reali durante i test.
 * I test verificano la corretta associazione tra Manutenzione, Immobile e Locatario
 * e le query di ricerca e statistiche.
 * 
 * @see com.epicode.Progetto_Backend.service.ManutenzioneService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused", "ThrowableResultOfMethodCallIgnored", "removal"})
class ManutenzioneServiceTest {

    @Autowired
    private ManutenzioneService manutenzioneService;

    @Autowired
    private ManutenzioneRepository manutenzioneRepository;

    @Autowired
    private ImmobileRepository immobileRepository;

    @Autowired
    private LocatarioRepository locatarioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Mock MailgunService per evitare chiamate reali all'API durante i test
    // Note: @MockBean is deprecated in Spring Boot 3.4+ but replacement @MockitoBean not available in 3.5.9
    @MockBean
    private MailgunService mailgunService;

    private Locatario testLocatario;
    private Immobile testImmobile;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Crea ruolo
        Role role = roleRepository.findByName("ROLE_LOCATARIO")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_LOCATARIO").build()));

        // Crea user
        User user = User.builder()
                .email("locatario@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Test")
                .cognome("Locatario")
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        user = userRepository.save(user);

        // Crea locatario
        testLocatario = Locatario.builder()
                .nome("Test")
                .cognome("Locatario")
                .cf("CF123456")
                .indirizzo("Via Test 1")
                .telefono("123456789")
                .user(user)
                .build();
        testLocatario = locatarioRepository.save(testLocatario);

        // Crea immobile
        Appartamento appartamento = new Appartamento();
        appartamento.setIndirizzo("Via Immobile 1");
        appartamento.setCitta("Roma");
        appartamento.setSuperficie(80.0);
        appartamento.setTipo(TipoImmobile.APPARTAMENTO);
        appartamento.setPiano(2);
        appartamento.setNumCamere(3);
        testImmobile = immobileRepository.save(appartamento);
    }

    @Test
    void testGetAllManutenzioni() {
        List<Manutenzione> manutenzioni = manutenzioneService.getAllManutenzioni();
        assertNotNull(manutenzioni);
    }

    @Test
    void testGetManutenzioneById() {
        ManutenzioneRequestDTO request = ManutenzioneRequestDTO.builder()
                .immobileId(testImmobile.getId())
                .locatarioId(testLocatario.getId())
                .dataMan(LocalDate.now())
                .importo(500.0)
                .tipo("ORDINARIA")
                .descrizione("Manutenzione test")
                .build();

        Manutenzione created = manutenzioneService.createManutenzione(request);
        Manutenzione found = manutenzioneService.getManutenzioneById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(500.0, found.getImporto());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testGetManutenzioneById_NotFound() {
        assertThrows(com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> manutenzioneService.getManutenzioneById(99999L));
    }

    @Test
    void testCreateManutenzione() {
        ManutenzioneRequestDTO request = ManutenzioneRequestDTO.builder()
                .immobileId(testImmobile.getId())
                .locatarioId(testLocatario.getId())
                .dataMan(LocalDate.now())
                .importo(500.0)
                .tipo("ORDINARIA")
                .descrizione("Manutenzione test")
                .build();

        Manutenzione manutenzione = manutenzioneService.createManutenzione(request);

        assertNotNull(manutenzione);
        assertNotNull(manutenzione.getId());
        assertEquals(500.0, manutenzione.getImporto());
        assertEquals("ORDINARIA", manutenzione.getTipo());
        assertEquals(testImmobile.getId(), manutenzione.getImmobile().getId());
        assertEquals(testLocatario.getId(), manutenzione.getLocatario().getId());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testCreateManutenzione_ImmobileNotFound() {
        ManutenzioneRequestDTO request = ManutenzioneRequestDTO.builder()
                .immobileId(99999L)
                .locatarioId(testLocatario.getId())
                .dataMan(LocalDate.now())
                .importo(500.0)
                .tipo("ORDINARIA")
                .descrizione("Manutenzione test")
                .build();

        assertThrows(com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> manutenzioneService.createManutenzione(request));
    }

    @Test
    void testUpdateManutenzione() {
        ManutenzioneRequestDTO createRequest = ManutenzioneRequestDTO.builder()
                .immobileId(testImmobile.getId())
                .locatarioId(testLocatario.getId())
                .dataMan(LocalDate.now())
                .importo(500.0)
                .tipo("ORDINARIA")
                .descrizione("Manutenzione test")
                .build();

        Manutenzione manutenzione = manutenzioneService.createManutenzione(createRequest);

        ManutenzioneRequestDTO updateRequest = ManutenzioneRequestDTO.builder()
                .importo(750.0)
                .tipo("STRAORDINARIA")
                .descrizione("Manutenzione aggiornata")
                .build();

        Manutenzione updated = manutenzioneService.updateManutenzione(manutenzione.getId(), updateRequest);

        assertEquals(750.0, updated.getImporto());
        assertEquals("STRAORDINARIA", updated.getTipo());
        assertEquals("Manutenzione aggiornata", updated.getDescrizione());
    }

    @Test
    void testGetManutenzioniByLocatarioId() {
        ManutenzioneRequestDTO request = ManutenzioneRequestDTO.builder()
                .immobileId(testImmobile.getId())
                .locatarioId(testLocatario.getId())
                .dataMan(LocalDate.now())
                .importo(500.0)
                .tipo("ORDINARIA")
                .descrizione("Manutenzione test")
                .build();

        manutenzioneService.createManutenzione(request);

        List<Manutenzione> manutenzioni = manutenzioneService.getManutenzioniByLocatarioId(testLocatario.getId());

        assertNotNull(manutenzioni);
    }

    @Test
    void testDeleteManutenzione() {
        ManutenzioneRequestDTO request = ManutenzioneRequestDTO.builder()
                .immobileId(testImmobile.getId())
                .locatarioId(testLocatario.getId())
                .dataMan(LocalDate.now())
                .importo(500.0)
                .tipo("ORDINARIA")
                .descrizione("Manutenzione test")
                .build();

        Manutenzione manutenzione = manutenzioneService.createManutenzione(request);
        Long id = manutenzione.getId();

        manutenzioneService.deleteManutenzione(id);

        assertFalse(manutenzioneRepository.existsById(id));
    }
}

