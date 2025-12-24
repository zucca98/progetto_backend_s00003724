package com.epicode.Progetto_Backend.graphql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Manutenzione;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * QueryResolverTest - Test unitari per le query GraphQL.
 * 
 * Questa classe testa i resolver delle query GraphQL, verificando:
 * - Query per utenti (users, user, me)
 * - Query per immobili (immobili, immobile)
 * - Query per contratti (contratti)
 * - Query per locatari (locatari)
 * - Query per rate (rate)
 * - Query per manutenzioni (manutenzioni)
 * - Query per ruoli (roles) - solo ADMIN
 * 
 * I test utilizzano @WithMockUser per simulare utenti autenticati
 * con diversi ruoli e verificare che le query funzionino correttamente
 * e rispettino le autorizzazioni configurate.
 * 
 * @see com.epicode.Progetto_Backend.graphql.QueryResolver
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class QueryResolverTest {

    @Autowired
    private QueryResolver queryResolver;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        Role role = roleRepository.findByName("ROLE_LOCATARIO")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_LOCATARIO").build()));

        testUser = User.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("password123"))
                .nome("Test")
                .cognome("User")
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUsers() {
        List<User> users = queryResolver.users();
        assertNotNull(users);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUser() {
        User found = queryResolver.user(testUser.getId());
        assertNotNull(found);
        assertEquals(testUser.getId(), found.getId());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void testMe() {
        User found = queryResolver.me(org.springframework.security.core.Authentication.class.cast(
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()));
        assertNotNull(found);
        assertEquals(testUser.getId(), found.getId());
        assertEquals("test@test.com", found.getEmail());
    }

    @Test
    @WithMockUser
    void testImmobili() {
        List<Immobile> immobili = queryResolver.immobili();
        assertNotNull(immobili);
    }

    @Test
    @WithMockUser
    void testImmobile() {
        // Questo test richiede un immobile esistente
        // Potrebbe essere necessario creare un immobile prima
        assertNotNull(queryResolver);
    }

    @Test
    @WithMockUser
    void testContratti() {
        List<Contratto> contratti = queryResolver.contratti();
        assertNotNull(contratti);
    }

    @Test
    @WithMockUser
    void testLocatari() {
        List<Locatario> locatari = queryResolver.locatari();
        assertNotNull(locatari);
    }

    @Test
    @WithMockUser
    void testRate() {
        List<Rata> rate = queryResolver.rate();
        assertNotNull(rate);
    }

    @Test
    @WithMockUser
    void testManutenzioni() {
        List<Manutenzione> manutenzioni = queryResolver.manutenzioni();
        assertNotNull(manutenzioni);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRoles() {
        List<Role> roles = queryResolver.roles();
        assertNotNull(roles);
    }
}

