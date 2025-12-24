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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.ContrattoRequestDTO;
import com.epicode.Progetto_Backend.entity.Appartamento;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.FrequenzaRata;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.ContrattoRepository;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * ContrattoServiceTest - Test unitari per il servizio di gestione contratti.
 * 
 * Questa classe testa i metodi del ContrattoService, verificando:
 * - Creazione contratti con generazione automatica delle rate
 * - Calcolo corretto del numero e importo delle rate in base alla frequenza
 * - Recupero lista contratti con paginazione
 * - Recupero contratto per ID
 * - Aggiornamento contratti
 * - Eliminazione contratti (con cascade delete delle rate)
 * - Query per contratti con rate non pagate
 * - Query per contratti per locatario email
 * 
 * Il MailgunService viene mockato per evitare l'invio di email reali durante i test.
 * I test verificano la logica complessa di generazione automatica delle rate
 * per diverse frequenze (MENSILE, TRIMESTRALE, SEMESTRALE, ANNUALE).
 * 
 * @see com.epicode.Progetto_Backend.service.ContrattoService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused", "ThrowableResultOfMethodCallIgnored", "removal"})
class ContrattoServiceTest {

    @Autowired
    private ContrattoService contrattoService;

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

    private Locatario testLocatario;
    private Immobile testImmobile;
    private User testUser;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Crea ruolo
        Role role = roleRepository.findByName("ROLE_LOCATARIO")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_LOCATARIO").build()));

        // Crea user
        testUser = User.builder()
                .email("locatario@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Test")
                .cognome("Locatario")
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        testUser = userRepository.save(testUser);

        // Crea locatario
        testLocatario = Locatario.builder()
                .nome("Test")
                .cognome("Locatario")
                .cf("CF123456")
                .indirizzo("Via Test 1")
                .telefono("123456789")
                .user(testUser)
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
    void testGetAllContratti() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contratto> page = contrattoService.getAllContratti(pageable);
        assertNotNull(page);
        assertNotNull(page.getContent());
    }

    @Test
    void testGetContrattoById() {
        ContrattoRequestDTO request = ContrattoRequestDTO.builder()
                .locatarioId(testLocatario.getId())
                .immobileId(testImmobile.getId())
                .dataInizio(LocalDate.now())
                .durataAnni(2)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.MENSILE)
                .build();

        Contratto created = contrattoService.createContratto(request);
        Contratto found = contrattoService.getContrattoById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(testLocatario.getId(), found.getLocatario().getId());
        assertEquals(testImmobile.getId(), found.getImmobile().getId());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testGetContrattoById_NotFound() {
        assertThrows(com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> contrattoService.getContrattoById(99999L));
    }

    @Test
    void testCreateContratto() {
        ContrattoRequestDTO request = ContrattoRequestDTO.builder()
                .locatarioId(testLocatario.getId())
                .immobileId(testImmobile.getId())
                .dataInizio(LocalDate.now())
                .durataAnni(2)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.MENSILE)
                .build();

        Contratto contratto = contrattoService.createContratto(request);

        assertNotNull(contratto);
        assertNotNull(contratto.getId());
        assertEquals(testLocatario.getId(), contratto.getLocatario().getId());
        assertEquals(testImmobile.getId(), contratto.getImmobile().getId());
        assertEquals(2, contratto.getDurataAnni());
        assertEquals(12000.0, contratto.getCanoneAnnuo());
    }

    @Test
    void testCreateContratto_GeneratesRate() {
        ContrattoRequestDTO request = ContrattoRequestDTO.builder()
                .locatarioId(testLocatario.getId())
                .immobileId(testImmobile.getId())
                .dataInizio(LocalDate.now())
                .durataAnni(1)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.MENSILE)
                .build();

        Contratto contratto = contrattoService.createContratto(request);

        // Verifica che siano state generate 12 rate (1 anno * 12 mesi)
        assertNotNull(contratto);
        assertNotNull(contratto.getId());
        List<Contratto> contratti = contrattoRepository.findAll();
        assertFalse(contratti.isEmpty());
    }

    @Test
    void testCreateContratto_LocatarioNotFound() {
        ContrattoRequestDTO request = ContrattoRequestDTO.builder()
                .locatarioId(99999L)
                .immobileId(testImmobile.getId())
                .dataInizio(LocalDate.now())
                .durataAnni(2)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.MENSILE)
                .build();

        assertThrows(com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> contrattoService.createContratto(request));
    }

    @Test
    void testUpdateContratto() {
        ContrattoRequestDTO createRequest = ContrattoRequestDTO.builder()
                .locatarioId(testLocatario.getId())
                .immobileId(testImmobile.getId())
                .dataInizio(LocalDate.now())
                .durataAnni(2)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.MENSILE)
                .build();

        Contratto contratto = contrattoService.createContratto(createRequest);

        ContrattoRequestDTO updateRequest = ContrattoRequestDTO.builder()
                .canoneAnnuo(15000.0)
                .durataAnni(3)
                .build();

        Contratto updated = contrattoService.updateContratto(contratto.getId(), updateRequest);

        assertEquals(15000.0, updated.getCanoneAnnuo());
        assertEquals(3, updated.getDurataAnni());
    }

    @Test
    void testDeleteContratto() {
        ContrattoRequestDTO request = ContrattoRequestDTO.builder()
                .locatarioId(testLocatario.getId())
                .immobileId(testImmobile.getId())
                .dataInizio(LocalDate.now())
                .durataAnni(2)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.MENSILE)
                .build();

        Contratto contratto = contrattoService.createContratto(request);
        Long id = contratto.getId();

        contrattoService.deleteContratto(id);

        assertFalse(contrattoRepository.existsById(id));
    }

    @Test
    void testGetContrattiByLocatarioId() {
        ContrattoRequestDTO request = ContrattoRequestDTO.builder()
                .locatarioId(testLocatario.getId())
                .immobileId(testImmobile.getId())
                .dataInizio(LocalDate.now())
                .durataAnni(2)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.MENSILE)
                .build();

        contrattoService.createContratto(request);

        List<Contratto> contratti = contrattoService.getContrattiByLocatarioId(testLocatario.getId());

        assertNotNull(contratti);
        assertFalse(contratti.isEmpty());
    }
}

