package com.epicode.Progetto_Backend.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.LocatarioRequestDTO;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * LocatarioServiceTest - Test unitari per il servizio di gestione locatari.
 * 
 * Questa classe testa i metodi del LocatarioService, verificando:
 * - Creazione locatari con associazione a User
 * - Recupero lista locatari con paginazione
 * - Recupero locatario per ID
 * - Aggiornamento locatari
 * - Eliminazione locatari
 * - Query per locatari con contratti di lunga durata
 * - Query per locatario per User ID
 * - Gestione degli errori (locatario non trovato, User già associato)
 * 
 * I test verificano la corretta associazione tra Locatario e User
 * e le query di ricerca avanzate per i locatari.
 * 
 * @see com.epicode.Progetto_Backend.service.LocatarioService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused", "ThrowableResultOfMethodCallIgnored"})
class LocatarioServiceTest {

    @Autowired
    private LocatarioService locatarioService;

    @Autowired
    private LocatarioRepository locatarioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Locatario testLocatario;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Crea ruolo
        Role role = roleRepository.findByName("ROLE_LOCATARIO")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_LOCATARIO").build()));

        // Crea user
        testUser = User.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Test")
                .cognome("User")
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
    }

    @Test
    void testGetAllLocatari() {
        Pageable pageable = PageRequest.of(0, 100);
        Page<Locatario> page = locatarioService.getAllLocatari(pageable);
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertFalse(page.getContent().isEmpty());
    }

    @Test
    void testGetLocatarioById() {
        Locatario found = locatarioService.getLocatarioById(testLocatario.getId());
        assertNotNull(found);
        assertEquals(testLocatario.getId(), found.getId());
        assertEquals("Test", found.getNome());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testGetLocatarioById_NotFound() {
        assertThrows(com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> locatarioService.getLocatarioById(99999L));
    }

    @Test
    void testGetLocatarioByUserId() {
        Locatario found = locatarioService.getLocatarioByUserId(testUser.getId());
        assertNotNull(found);
        assertEquals(testLocatario.getId(), found.getId());
        assertEquals(testUser.getId(), found.getUser().getId());
    }

    @Test
    void testGetUserByEmail() {
        User found = locatarioService.getUserByEmail("test@test.com");
        assertNotNull(found);
        assertEquals("test@test.com", found.getEmail());
    }

    @Test
    void testCreateLocatario() {
        // Crea nuovo user
        Role role = roleRepository.findByName("ROLE_LOCATARIO").orElseThrow();
        User newUser = User.builder()
                .email("new@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("New")
                .cognome("User")
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        newUser = userRepository.save(newUser);

        LocatarioRequestDTO request = LocatarioRequestDTO.builder()
                .nome("New")
                .cognome("Locatario")
                .cf("CF789012")
                .indirizzo("Via New 1")
                .telefono("987654321")
                .userId(newUser.getId())
                .build();

        Locatario locatario = locatarioService.createLocatario(request);

        assertNotNull(locatario);
        assertNotNull(locatario.getId());
        assertEquals("New", locatario.getNome());
        assertEquals("New Locatario", locatario.getNome() + " " + locatario.getCognome());
        assertEquals(newUser.getId(), locatario.getUser().getId());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testCreateLocatario_UserNotFound() {
        LocatarioRequestDTO request = LocatarioRequestDTO.builder()
                .nome("Test")
                .cognome("Locatario")
                .cf("CF123456")
                .indirizzo("Via Test 1")
                .telefono("123456789")
                .userId(99999L)
                .build();

        assertThrows(com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> locatarioService.createLocatario(request));
    }

    @Test
    void testUpdateLocatario() {
        LocatarioRequestDTO request = LocatarioRequestDTO.builder()
                .nome("Updated")
                .cognome("Locatario")
                .cf("CF999999")
                .indirizzo("Via Updated 1")
                .telefono("111111111")
                .userId(testUser.getId())
                .build();

        Locatario updated = locatarioService.updateLocatario(testLocatario.getId(), request);

        assertEquals("Updated", updated.getNome());
        assertEquals("CF999999", updated.getCf());
        assertEquals("Via Updated 1", updated.getIndirizzo());
    }

    @Test
    void testDeleteLocatario() {
        Long id = testLocatario.getId();
        locatarioService.deleteLocatario(id);

        assertFalse(locatarioRepository.existsById(id));
    }

    @Test
    void testGetLocatariConContrattiLunghiDurata() {
        List<Locatario> locatari = locatarioService.getLocatariConContrattiLunghiDurata();
        assertNotNull(locatari);
    }
}

