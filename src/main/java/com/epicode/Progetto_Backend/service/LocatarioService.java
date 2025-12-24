package com.epicode.Progetto_Backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.epicode.Progetto_Backend.dto.LocatarioRequestDTO;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.exception.EntityNotFoundException;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * LocatarioService - Servizio per la gestione dei locatari.
 * 
 * Gestisce tutte le operazioni CRUD sui locatari:
 * - Recupero locatari (lista paginata, per ID, per userId)
 * - Creazione locatari con associazione a User
 * - Aggiornamento locatari
 * - Eliminazione locatari
 * - Query personalizzate (locatari con contratti lunghi)
 * 
 * Caratteristiche:
 * - Ogni locatario deve essere associato a un User esistente
 * - Il codice fiscale (cf) deve essere univoco
 * - Fornisce metodi per recuperare locatari tramite email utente
 * 
 * Utilizzato da:
 * - LocatarioController per gli endpoint REST
 * - MutationResolver per le mutation GraphQL
 * 
 * @see com.epicode.Progetto_Backend.entity.Locatario
 * @see com.epicode.Progetto_Backend.entity.User
 */
@Service
@RequiredArgsConstructor
public class LocatarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocatarioService.class);
    
    private final LocatarioRepository locatarioRepository;
    private final UserRepository userRepository;
    
    public User getUserByEmail(String email) {
        logger.debug("Recupero user per email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User non trovato con email: {}", email);
                    return new EntityNotFoundException("User", email);
                });
    }
    
    public Page<Locatario> getAllLocatari(Pageable pageable) {
        logger.debug("Recupero locatari paginati. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Locatario> page = locatarioRepository.findAll(pageable);
        logger.info("Recuperati {} locatari su {} totali", page.getNumberOfElements(), page.getTotalElements());
        return page;
    }
    
    @Deprecated
    public List<Locatario> getAllLocatari() {
        logger.debug("Recupero di tutti i locatari (deprecated - use paginated version)");
        List<Locatario> locatari = locatarioRepository.findAll();
        logger.info("Recuperati {} locatari", locatari.size());
        return locatari;
    }
    
    @SuppressWarnings("null")
    public Locatario getLocatarioById(Long id) {
        logger.debug("Recupero locatario con ID: {}", id);
        return locatarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Locatario non trovato con ID: {}", id);
                    return new EntityNotFoundException("Locatario", id);
                });
    }
    
    public Locatario getLocatarioByUserId(Long userId) {
        logger.debug("Recupero locatario per user ID: {}", userId);
        return locatarioRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Locatario non trovato per user ID: {}", userId);
                    return new EntityNotFoundException("Locatario", "user: " + userId);
                });
    }
    
    @SuppressWarnings("null")
    @Transactional
    public Locatario createLocatario(LocatarioRequestDTO request) {
        logger.info("Creazione nuovo locatario. Nome: {}, Cognome: {}, User ID: {}", 
                request.getNome(), request.getCognome(), request.getUserId());
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    logger.error("User non trovato con ID: {}", request.getUserId());
                    return new EntityNotFoundException("User", request.getUserId());
                });
        
        Locatario locatario = Locatario.builder()
                .nome(request.getNome())
                .cognome(request.getCognome())
                .cf(request.getCf())
                .indirizzo(request.getIndirizzo())
                .telefono(request.getTelefono())
                .user(user)
                .build();
        
        Locatario saved = locatarioRepository.save(locatario);
        logger.info("Locatario creato con successo. ID: {}", saved.getId());
        return saved;
    }
    
    @Transactional
    public Locatario updateLocatario(Long id, LocatarioRequestDTO request) {
        logger.info("Aggiornamento locatario con ID: {}", id);
        Locatario locatario = getLocatarioById(id);
        
        locatario.setNome(request.getNome());
        locatario.setCognome(request.getCognome());
        locatario.setCf(request.getCf());
        locatario.setIndirizzo(request.getIndirizzo());
        locatario.setTelefono(request.getTelefono());
        
        Locatario updated = locatarioRepository.save(locatario);
        logger.info("Locatario aggiornato con successo. ID: {}", id);
        return updated;
    }
    
    @SuppressWarnings("null")
    @Transactional
    public void deleteLocatario(Long id) {
        logger.info("Eliminazione locatario con ID: {}", id);
        if (!locatarioRepository.existsById(id)) {
            logger.warn("Tentativo di eliminare locatario inesistente con ID: {}", id);
            throw new EntityNotFoundException("Locatario", id);
        }
        locatarioRepository.deleteById(id);
        logger.info("Locatario eliminato con successo. ID: {}", id);
    }
    
    // Query custom
    public List<Locatario> getLocatariConContrattiLunghiDurata() {
        logger.debug("Recupero locatari con contratti di lunga durata");
        return locatarioRepository.findLocatariConContrattiLunghiDurata();
    }
}
