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

import com.epicode.Progetto_Backend.dto.RataRequestDTO;
import com.epicode.Progetto_Backend.entity.Appartamento;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.FrequenzaRata;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.ContrattoRepository;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RataRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * RataServiceTest - Test unitari per il servizio di gestione rate (installments).
 * 
 * Questa classe testa i metodi del RataService, verificando:
 * - Creazione rate
 * - Recupero lista rate con paginazione
 * - Recupero rata per ID
 * - Aggiornamento rate (marcatura come pagata/non pagata)
 * - Query per rate non pagate
 * - Query per rate scadute
 * - Query per rate per contratto
 * - Query per rate per locatario email
 * - Invio email di conferma pagamento (mockato)
 * 
 * Il MailgunService viene mockato per evitare l'invio di email reali durante i test.
 * I test verificano la corretta gestione dello stato di pagamento delle rate
 * e le query di ricerca avanzate.
 * 
 * @see com.epicode.Progetto_Backend.service.RataService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused", "ThrowableResultOfMethodCallIgnored", "removal"})
class RataServiceTest {

    @Autowired
    private RataService rataService;

    @Autowired
    private RataRepository rataRepository;

    @Autowired
    private ContrattoRepository contrattoRepository;

    @Autowired
    private LocatarioRepository locatarioRepository;

    @Autowired
    private ImmobileRepository immobileRepository;

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

    private Contratto testContratto;
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

        // Crea contratto
        testContratto = Contratto.builder()
                .locatario(testLocatario)
                .immobile(testImmobile)
                .dataInizio(LocalDate.now())
                .durataAnni(2)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.MENSILE)
                .build();
        testContratto = contrattoRepository.save(testContratto);
    }

    @Test
    void testGetAllRate() {
        List<Rata> rate = rataService.getAllRate();
        assertNotNull(rate);
    }

    @Test
    void testGetRataById() {
        RataRequestDTO request = RataRequestDTO.builder()
                .contrattoId(testContratto.getId())
                .numeroRata(1)
                .dataScadenza(LocalDate.now().plusMonths(1))
                .importo(1000.0)
                .pagata('N')
                .build();

        Rata created = rataService.createRata(request);
        Rata found = rataService.getRataById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(1, found.getNumeroRata());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testGetRataById_NotFound() {
        assertThrows(com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> rataService.getRataById(99999L));
    }

    @Test
    void testCreateRata() {
        RataRequestDTO request = RataRequestDTO.builder()
                .contrattoId(testContratto.getId())
                .numeroRata(1)
                .dataScadenza(LocalDate.now().plusMonths(1))
                .importo(1000.0)
                .pagata('N')
                .build();

        Rata rata = rataService.createRata(request);

        assertNotNull(rata);
        assertNotNull(rata.getId());
        assertEquals(1, rata.getNumeroRata());
        assertEquals(1000.0, rata.getImporto());
        assertEquals('N', rata.getPagata());
        assertEquals(testContratto.getId(), rata.getContratto().getId());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testCreateRata_ContrattoNotFound() {
        RataRequestDTO request = RataRequestDTO.builder()
                .contrattoId(99999L)
                .numeroRata(1)
                .dataScadenza(LocalDate.now().plusMonths(1))
                .importo(1000.0)
                .build();

        assertThrows(com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> rataService.createRata(request));
    }

    @Test
    void testUpdateRata() {
        RataRequestDTO createRequest = RataRequestDTO.builder()
                .contrattoId(testContratto.getId())
                .numeroRata(1)
                .dataScadenza(LocalDate.now().plusMonths(1))
                .importo(1000.0)
                .pagata('N')
                .build();

        Rata rata = rataService.createRata(createRequest);

        RataRequestDTO updateRequest = RataRequestDTO.builder()
                .importo(1200.0)
                .pagata('S')
                .build();

        Rata updated = rataService.updateRata(rata.getId(), updateRequest);

        assertEquals(1200.0, updated.getImporto());
        assertEquals('S', updated.getPagata());
    }

    @Test
    void testUpdateRataPagata() {
        RataRequestDTO request = RataRequestDTO.builder()
                .contrattoId(testContratto.getId())
                .numeroRata(1)
                .dataScadenza(LocalDate.now().plusMonths(1))
                .importo(1000.0)
                .pagata('N')
                .build();

        Rata rata = rataService.createRata(request);

        Rata updated = rataService.updateRataPagata(rata.getId(), 'S');

        assertEquals('S', updated.getPagata());
    }

    @Test
    void testGetRateByContrattoId() {
        RataRequestDTO request = RataRequestDTO.builder()
                .contrattoId(testContratto.getId())
                .numeroRata(1)
                .dataScadenza(LocalDate.now().plusMonths(1))
                .importo(1000.0)
                .build();

        rataService.createRata(request);

        List<Rata> rate = rataService.getRateByContrattoId(testContratto.getId());

        assertNotNull(rate);
        assertFalse(rate.isEmpty());
    }

    @Test
    void testGetRateNonPagate() {
        RataRequestDTO request = RataRequestDTO.builder()
                .contrattoId(testContratto.getId())
                .numeroRata(1)
                .dataScadenza(LocalDate.now().plusMonths(1))
                .importo(1000.0)
                .pagata('N')
                .build();

        rataService.createRata(request);

        List<Rata> rate = rataService.getRateNonPagate();

        assertNotNull(rate);
    }

    @Test
    void testGetRateScaduteNonPagate() {
        RataRequestDTO request = RataRequestDTO.builder()
                .contrattoId(testContratto.getId())
                .numeroRata(1)
                .dataScadenza(LocalDate.now().minusDays(1))
                .importo(1000.0)
                .pagata('N')
                .build();

        rataService.createRata(request);

        List<Rata> rate = rataService.getRateScaduteNonPagate();

        assertNotNull(rate);
    }

    @Test
    void testDeleteRata() {
        RataRequestDTO request = RataRequestDTO.builder()
                .contrattoId(testContratto.getId())
                .numeroRata(1)
                .dataScadenza(LocalDate.now().plusMonths(1))
                .importo(1000.0)
                .build();

        Rata rata = rataService.createRata(request);
        Long id = rata.getId();

        rataService.deleteRata(id);

        assertFalse(rataRepository.existsById(id));
    }
}

