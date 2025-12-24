package com.epicode.Progetto_Backend.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.entity.Appartamento;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.FrequenzaRata;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Negozio;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.Ufficio;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.ContrattoRepository;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RataRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * FieldResolverTest - Test unitari per i field resolver GraphQL custom.
 * 
 * Questa classe testa i field resolver personalizzati che gestiscono:
 * - Risoluzione di tipi polimorfici Immobile (Appartamento, Negozio, Ufficio)
 * - Conversione del campo 'pagata' da carattere (S/N) a Boolean
 * - Accesso a campi specifici degli immobili specializzati
 * 
 * I field resolver sono necessari per gestire correttamente l'ereditarietà
 * delle classi Immobile nel contesto GraphQL, dove GraphQL non supporta
 * nativamente l'ereditarietà Java.
 * 
 * @see com.epicode.Progetto_Backend.graphql.FieldResolver
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class FieldResolverTest {

    @Autowired
    private FieldResolver fieldResolver;

    @Autowired
    private ImmobileRepository immobileRepository;

    @Autowired
    private RataRepository rataRepository;

    @Autowired
    private ContrattoRepository contrattoRepository;

    @Autowired
    private LocatarioRepository locatarioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Appartamento testAppartamento;
    private Negozio testNegozio;
    private Ufficio testUfficio;
    private Contratto testContratto;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Crea ruolo se non esiste
        Role role = roleRepository.findByName("ROLE_LOCATARIO")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_LOCATARIO").build()));

        // Crea user
        User user = User.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Test")
                .cognome("User")
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        user = userRepository.save(user);

        // Crea locatario
        Locatario locatario = Locatario.builder()
                .nome("Test")
                .cognome("Locatario")
                .cf("CF123456")
                .indirizzo("Via Test 1")
                .telefono("123456789")
                .user(user)
                .build();
        locatario = locatarioRepository.save(locatario);

        // Crea un appartamento
        testAppartamento = new Appartamento();
        testAppartamento.setIndirizzo("Via Test Appartamento 1");
        testAppartamento.setCitta("Roma");
        testAppartamento.setSuperficie(80.0);
        testAppartamento.setTipo(TipoImmobile.APPARTAMENTO);
        testAppartamento.setPiano(2);
        testAppartamento.setNumCamere(3);
        testAppartamento = (Appartamento) immobileRepository.save(testAppartamento);

        // Crea un negozio
        testNegozio = new Negozio();
        testNegozio.setIndirizzo("Via Test Negozio 1");
        testNegozio.setCitta("Milano");
        testNegozio.setSuperficie(100.0);
        testNegozio.setTipo(TipoImmobile.NEGOZIO);
        testNegozio.setVetrine(3);
        testNegozio.setMagazzinoMq(50.0);
        testNegozio = (Negozio) immobileRepository.save(testNegozio);

        // Crea un ufficio
        testUfficio = new Ufficio();
        testUfficio.setIndirizzo("Via Test Ufficio 1");
        testUfficio.setCitta("Torino");
        testUfficio.setSuperficie(150.0);
        testUfficio.setTipo(TipoImmobile.UFFICIO);
        testUfficio.setPostiLavoro(10);
        testUfficio.setSaleRiunioni(2);
        testUfficio = (Ufficio) immobileRepository.save(testUfficio);

        // Crea contratto per i test delle rate
        testContratto = Contratto.builder()
                .locatario(locatario)
                .immobile(testAppartamento)
                .dataInizio(java.time.LocalDate.now())
                .durataAnni(2)
                .canoneAnnuo(12000.0)
                .frequenzaRata(FrequenzaRata.MENSILE)
                .build();
        testContratto = contrattoRepository.save(testContratto);
    }

    @Test
    void testAppartamentoFieldResolver() {
        Appartamento result = fieldResolver.appartamento(testAppartamento);
        assertNotNull(result);
        assertEquals(testAppartamento.getId(), result.getId());
        assertEquals("Via Test Appartamento 1", result.getIndirizzo());
    }

    @Test
    void testAppartamentoFieldResolverWithNonAppartamento() {
        Appartamento result = fieldResolver.appartamento(testNegozio);
        assertNull(result);
    }

    @Test
    void testUfficioFieldResolver() {
        Ufficio result = fieldResolver.ufficio(testUfficio);
        assertNotNull(result);
        assertEquals(testUfficio.getId(), result.getId());
        assertEquals("Via Test Ufficio 1", result.getIndirizzo());
    }

    @Test
    void testUfficioFieldResolverWithNonUfficio() {
        Ufficio result = fieldResolver.ufficio(testAppartamento);
        assertNull(result);
    }

    @Test
    void testNegozioFieldResolver() {
        Negozio result = fieldResolver.negozio(testNegozio);
        assertNotNull(result);
        assertEquals(testNegozio.getId(), result.getId());
        assertEquals("Via Test Negozio 1", result.getIndirizzo());
    }

    @Test
    void testNegozioFieldResolverWithNonNegozio() {
        Negozio result = fieldResolver.negozio(testAppartamento);
        assertNull(result);
    }

    @Test
    void testPagataFieldResolverWithPagata() {
        // Crea una rata pagata
        Rata rataPagata = new Rata();
        rataPagata.setContratto(testContratto);
        rataPagata.setPagata('S');
        rataPagata.setImporto(1000.0);
        rataPagata.setNumeroRata(1);
        rataPagata.setDataScadenza(java.time.LocalDate.now());
        rataPagata = rataRepository.save(rataPagata);

        Boolean result = fieldResolver.pagata(rataPagata);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void testPagataFieldResolverWithNonPagata() {
        // Crea una rata non pagata
        Rata rataNonPagata = new Rata();
        rataNonPagata.setContratto(testContratto);
        rataNonPagata.setPagata('N');
        rataNonPagata.setImporto(1000.0);
        rataNonPagata.setNumeroRata(1);
        rataNonPagata.setDataScadenza(java.time.LocalDate.now());
        rataNonPagata = rataRepository.save(rataNonPagata);

        Boolean result = fieldResolver.pagata(rataNonPagata);
        assertNotNull(result);
        assertEquals(false, result);
    }

    @Test
    void testAppartamentoIdFieldResolver() {
        Long result = fieldResolver.appartamentoId(testAppartamento);
        assertNotNull(result);
        assertEquals(testAppartamento.getId(), result);
    }

    @Test
    void testAppartamentoIndirizzoFieldResolver() {
        String result = fieldResolver.appartamentoIndirizzo(testAppartamento);
        assertNotNull(result);
        assertEquals("Via Test Appartamento 1", result);
    }

    @Test
    void testAppartamentoCittaFieldResolver() {
        String result = fieldResolver.appartamentoCitta(testAppartamento);
        assertNotNull(result);
        assertEquals("Roma", result);
    }

    @Test
    void testAppartamentoSuperficieFieldResolver() {
        Double result = fieldResolver.appartamentoSuperficie(testAppartamento);
        assertNotNull(result);
        assertEquals(80.0, result);
    }

    @Test
    void testAppartamentoTipoFieldResolver() {
        TipoImmobile result = fieldResolver.appartamentoTipo(testAppartamento);
        assertNotNull(result);
        assertEquals(TipoImmobile.APPARTAMENTO, result);
    }

    @Test
    void testUfficioIdFieldResolver() {
        Long result = fieldResolver.ufficioId(testUfficio);
        assertNotNull(result);
        assertEquals(testUfficio.getId(), result);
    }

    @Test
    void testUfficioIndirizzoFieldResolver() {
        String result = fieldResolver.ufficioIndirizzo(testUfficio);
        assertNotNull(result);
        assertEquals("Via Test Ufficio 1", result);
    }

    @Test
    void testUfficioCittaFieldResolver() {
        String result = fieldResolver.ufficioCitta(testUfficio);
        assertNotNull(result);
        assertEquals("Torino", result);
    }

    @Test
    void testUfficioSuperficieFieldResolver() {
        Double result = fieldResolver.ufficioSuperficie(testUfficio);
        assertNotNull(result);
        assertEquals(150.0, result);
    }

    @Test
    void testUfficioTipoFieldResolver() {
        TipoImmobile result = fieldResolver.ufficioTipo(testUfficio);
        assertNotNull(result);
        assertEquals(TipoImmobile.UFFICIO, result);
    }

    @Test
    void testNegozioIdFieldResolver() {
        Long result = fieldResolver.negozioId(testNegozio);
        assertNotNull(result);
        assertEquals(testNegozio.getId(), result);
    }

    @Test
    void testNegozioIndirizzoFieldResolver() {
        String result = fieldResolver.negozioIndirizzo(testNegozio);
        assertNotNull(result);
        assertEquals("Via Test Negozio 1", result);
    }

    @Test
    void testNegozioCittaFieldResolver() {
        String result = fieldResolver.negozioCitta(testNegozio);
        assertNotNull(result);
        assertEquals("Milano", result);
    }

    @Test
    void testNegozioSuperficieFieldResolver() {
        Double result = fieldResolver.negozioSuperficie(testNegozio);
        assertNotNull(result);
        assertEquals(100.0, result);
    }

    @Test
    void testNegozioTipoFieldResolver() {
        TipoImmobile result = fieldResolver.negozioTipo(testNegozio);
        assertNotNull(result);
        assertEquals(TipoImmobile.NEGOZIO, result);
    }
}
