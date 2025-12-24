package com.epicode.Progetto_Backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.UserUpdateDTO;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.exception.EntityNotFoundException;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * UserService - Servizio per la gestione degli utenti.
 * 
 * Gestisce tutte le operazioni CRUD sugli utenti:
 * - Recupero utenti (lista, per ID, per email)
 * - Aggiornamento profilo utente (nome, cognome, immagine profilo)
 * - Aggiornamento ruoli utente
 * - Eliminazione utenti
 * 
 * Caratteristiche:
 * - I campi opzionali vengono aggiornati solo se non null
 * - I ruoli vengono normalizzati (aggiunge prefisso ROLE_ se mancante)
 * - L'immagine profilo può essere aggiornata tramite URL (solitamente da Cloudinary)
 * 
 * Utilizzato da:
 * - UserController per gli endpoint REST
 * - MutationResolver per le mutation GraphQL
 * 
 * @see com.epicode.Progetto_Backend.controller.UserController
 * @see com.epicode.Progetto_Backend.graphql.MutationResolver
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public List<User> getAllUsers() {
        logger.debug("Recupero di tutti gli utenti");
        List<User> users = userRepository.findAll();
        logger.info("Recuperati {} utenti", users.size());
        return users;
    }

    @SuppressWarnings("null")
    public User getUserById(Long id) {
        logger.debug("Recupero utente con ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Utente non trovato con ID: {}", id);
                    return new EntityNotFoundException("User", id);
                });
    }

    public User getUserByEmail(String email) {
        logger.debug("Recupero utente con email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Utente non trovato con email: {}", email);
                    return new EntityNotFoundException("User", email);
                });
    }

    @SuppressWarnings("null")
    @Transactional
    public User updateUser(Long id, UserUpdateDTO request) {
        logger.info("Aggiornamento utente con ID: {}", id);
        User user = getUserById(id);

        if (request.getNome() != null && !request.getNome().isBlank()) {
            user.setNome(request.getNome());
            logger.debug("Nome aggiornato per utente ID: {}", id);
        }
        if (request.getCognome() != null && !request.getCognome().isBlank()) {
            user.setCognome(request.getCognome());
            logger.debug("Cognome aggiornato per utente ID: {}", id);
        }
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
            logger.debug("Immagine profilo aggiornata per utente ID: {}", id);
        }

        User updated = userRepository.save(user);
        logger.info("Utente aggiornato con successo. ID: {}, Email: {}", updated.getId(), updated.getEmail());
        return updated;
    }

    @Transactional
    public User updateProfileImage(Long id, String imageUrl) {
        User user = getUserById(id);
        user.setProfileImage(imageUrl);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfileImageByEmail(String email, String imageUrl) {
        User user = getUserByEmail(email);
        user.setProfileImage(imageUrl);
        return userRepository.save(user);
    }

    @SuppressWarnings("null")
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Eliminazione utente con ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("Tentativo di eliminare utente inesistente con ID: {}", id);
            throw new EntityNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        logger.info("Utente eliminato con successo. ID: {}", id);
    }

    @Transactional
    public User updateUserRoles(Long id, Set<String> roleNames) {
        logger.info("Aggiornamento ruoli per utente ID: {}, ruoli: {}", id, roleNames);
        User user = getUserById(id);

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            // Normalizza il nome del ruolo (aggiungi ROLE_ se mancante)
            String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(() -> {
                        logger.error("Ruolo non trovato: {}", normalizedRoleName);
                        return new EntityNotFoundException("Role", normalizedRoleName);
                    });
            roles.add(role);
        }

        user.setRoles(roles);
        User updated = userRepository.save(user);
        logger.info("Ruoli aggiornati con successo per utente ID: {}", id);
        return updated;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
