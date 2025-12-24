package com.epicode.Progetto_Backend.service;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.UserUpdateDTO;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * UserServiceTest - Test unitari per il servizio di gestione utenti.
 * 
 * Questa classe testa i metodi del UserService, verificando:
 * - Recupero di tutti gli utenti
 * - Recupero utente per ID ed email
 * - Aggiornamento dati utente
 * - Gestione ruoli (assegnazione, normalizzazione nomi ruoli)
 * - Eliminazione utenti
 * - Gestione degli errori (utente non trovato)
 * 
 * I test utilizzano un database H2 in-memory per isolare i test dal
 * database di produzione. Ogni test viene eseguito in una transazione
 * che viene rollbackata alla fine per garantire l'indipendenza dei test.
 * 
 * @see com.epicode.Progetto_Backend.service.UserService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings({"null", "unused", "ThrowableResultOfMethodCallIgnored"})
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    @SuppressWarnings("unused")
    private Role adminRole;
    @SuppressWarnings("unused")
    private Role managerRole;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Crea ruoli
        adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        managerRole = roleRepository.findByName("ROLE_MANAGER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_MANAGER").build()));

        // Crea utente di test
        testUser = User.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Test")
                .cognome("User")
                .enabled(true)
                .roles(new HashSet<>())
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void testGetAllUsers() {
        var users = userService.getAllUsers();
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    void testGetUserById() {
        User found = userService.getUserById(testUser.getId());
        assertNotNull(found);
        assertEquals(testUser.getId(), found.getId());
        assertEquals("test@test.com", found.getEmail());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testGetUserById_NotFound() {
        assertThrows(com.epicode.Progetto_Backend.exception.EntityNotFoundException.class, 
                () -> userService.getUserById(99999L));
    }

    @Test
    void testGetUserByEmail() {
        User found = userService.getUserByEmail("test@test.com");
        assertNotNull(found);
        assertEquals("test@test.com", found.getEmail());
    }

    @Test
    void testUpdateUser() {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setNome("Updated");
        updateDTO.setCognome("Name");

        User updated = userService.updateUser(testUser.getId(), updateDTO);

        assertEquals("Updated", updated.getNome());
        assertEquals("Name", updated.getCognome());
    }

    @Test
    void testUpdateUserRoles() {
        Set<String> newRoles = Set.of("ROLE_ADMIN", "ROLE_MANAGER");

        User updated = userService.updateUserRoles(testUser.getId(), newRoles);

        assertEquals(2, updated.getRoles().size());
        assertTrue(updated.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN")));
        assertTrue(updated.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_MANAGER")));
    }

    @Test
    void testUpdateUserRoles_NormalizeRoleName() {
        // Testa che "ADMIN" venga normalizzato a "ROLE_ADMIN"
        Set<String> roles = Set.of("ADMIN");

        User updated = userService.updateUserRoles(testUser.getId(), roles);

        assertTrue(updated.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN")));
    }

    @Test
    void testGetAllRoles() {
        var roles = userService.getAllRoles();
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
    }

    @Test
    void testDeleteUser() {
        Long userId = testUser.getId();
        userService.deleteUser(userId);

        assertFalse(userRepository.existsById(userId));
    }
}

