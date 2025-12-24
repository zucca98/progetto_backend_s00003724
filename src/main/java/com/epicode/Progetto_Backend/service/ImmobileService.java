package com.epicode.Progetto_Backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.epicode.Progetto_Backend.dto.ImmobileRequestDTO;
import com.epicode.Progetto_Backend.entity.Appartamento;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Negozio;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.Ufficio;
import com.epicode.Progetto_Backend.exception.EntityNotFoundException;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;

/**
 * ImmobileService - Servizio per la gestione degli immobili.
 * 
 * Gestisce tutte le operazioni CRUD sugli immobili:
 * - Recupero immobili (lista paginata, per ID)
 * - Creazione immobili con gestione dei sottotipi (Appartamento, Negozio, Ufficio)
 * - Aggiornamento immobili con aggiornamento campi specifici per tipo
 * - Eliminazione immobili
 * - Statistiche immobili (per città, per tipo)
 * 
 * Caratteristiche:
 * - Utilizza ereditarietà JPA JOINED per gestire i sottotipi
 * - Alla creazione, istanzia la classe corretta in base al tipo
 * - All'aggiornamento, aggiorna solo i campi specifici del tipo
 * 
 * Utilizzato da:
 * - ImmobileController per gli endpoint REST
 * - MutationResolver per le mutation GraphQL
 * 
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see com.epicode.Progetto_Backend.entity.Appartamento
 * @see com.epicode.Progetto_Backend.entity.Negozio
 * @see com.epicode.Progetto_Backend.entity.Ufficio
 */
@Service
@RequiredArgsConstructor
public class ImmobileService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImmobileService.class);
    
    private final ImmobileRepository immobileRepository;
    
    public Page<Immobile> getAllImmobili(Pageable pageable) {
        logger.debug("Recupero immobili paginati. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Immobile> page = immobileRepository.findAll(pageable);
        logger.info("Recuperati {} immobili su {} totali", page.getNumberOfElements(), page.getTotalElements());
        return page;
    }
    
    @Deprecated
    public List<Immobile> getAllImmobili() {
        logger.debug("Recupero di tutti gli immobili (deprecated - use paginated version)");
        List<Immobile> immobili = immobileRepository.findAll();
        logger.info("Recuperati {} immobili", immobili.size());
        return immobili;
    }
    
    @SuppressWarnings("null")
    public Immobile getImmobileById(Long id) {
        logger.debug("Recupero immobile con ID: {}", id);
        return immobileRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Immobile non trovato con ID: {}", id);
                    return new EntityNotFoundException("Immobile", id);
                });
    }
    
    /**
     * Crea un nuovo immobile con il tipo specificato.
     * 
     * In base al tipo (APPARTAMENTO, NEGOZIO, UFFICIO), viene istanziata
     * la classe corretta e vengono impostati i campi specifici.
     * 
     * @param request DTO con dati dell'immobile da creare
     * @return Immobile creato con ID assegnato
     */
    @Transactional
    public Immobile createImmobile(ImmobileRequestDTO request) {
        logger.info("Creazione nuovo immobile. Tipo: {}, Indirizzo: {}", request.getTipo(), request.getIndirizzo());
        Immobile immobile = switch (request.getTipo()) {
            case APPARTAMENTO -> {
                Appartamento app = new Appartamento();
                app.setPiano(request.getPiano());
                app.setNumCamere(request.getNumCamere());
                yield app;
            }
            case NEGOZIO -> {
                Negozio neg = new Negozio();
                neg.setVetrine(request.getVetrine());
                neg.setMagazzinoMq(request.getMagazzinoMq());
                yield neg;
            }
            case UFFICIO -> {
                Ufficio uff = new Ufficio();
                uff.setPostiLavoro(request.getPostiLavoro());
                uff.setSaleRiunioni(request.getSaleRiunioni());
                yield uff;
            }
        };
        
        immobile.setIndirizzo(request.getIndirizzo());
        immobile.setCitta(request.getCitta());
        immobile.setSuperficie(request.getSuperficie());
        immobile.setTipo(request.getTipo());
        
        Immobile saved = immobileRepository.save(immobile);
        logger.info("Immobile creato con successo. ID: {}, Tipo: {}", saved.getId(), saved.getTipo());
        return saved;
    }
    
    @SuppressWarnings("null")
    @Transactional
    public Immobile updateImmobile(Long id, ImmobileRequestDTO request) {
        logger.info("Aggiornamento immobile con ID: {}", id);
        Immobile immobile = getImmobileById(id);

        if (request.getIndirizzo() != null && !request.getIndirizzo().isBlank()) {
            immobile.setIndirizzo(request.getIndirizzo());
        }
        if (request.getCitta() != null && !request.getCitta().isBlank()) {
            immobile.setCitta(request.getCitta());
        }
        if (request.getSuperficie() != null) {
            immobile.setSuperficie(request.getSuperficie());
        }

        // Aggiorna campi specifici per tipo
        switch (immobile) {
            case Appartamento app -> {
                if (request.getPiano() != null) {
                    app.setPiano(request.getPiano());
                }
                if (request.getNumCamere() != null) {
                    app.setNumCamere(request.getNumCamere());
                }
            }
            case Negozio neg -> {
                if (request.getVetrine() != null) {
                    neg.setVetrine(request.getVetrine());
                }
                if (request.getMagazzinoMq() != null) {
                    neg.setMagazzinoMq(request.getMagazzinoMq());
                }
            }
            case Ufficio uff -> {
                if (request.getPostiLavoro() != null) {
                    uff.setPostiLavoro(request.getPostiLavoro());
                }
                if (request.getSaleRiunioni() != null) {
                    uff.setSaleRiunioni(request.getSaleRiunioni());
                }
            }
            default -> {
                // Nessuna azione per altri tipi
            }
        }

        Immobile updated = immobileRepository.save(immobile);
        logger.info("Immobile aggiornato con successo. ID: {}", id);
        return updated;
    }

    @SuppressWarnings("null")
    @Transactional
    public void deleteImmobile(Long id) {
        logger.info("Eliminazione immobile con ID: {}", id);
        if (!immobileRepository.existsById(id)) {
            logger.warn("Tentativo di eliminare immobile inesistente con ID: {}", id);
            throw new EntityNotFoundException("Immobile", id);
        }
        immobileRepository.deleteById(id);
        logger.info("Immobile eliminato con successo. ID: {}", id);
    }

    // Query custom
    public Map<String, Long> getImmobiliAffittatiPerCitta() {
        logger.debug("Recupero statistiche immobili affittati per città");
        List<Object[]> results = immobileRepository.countImmobiliAffittatiPerCitta();
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put((String) result[0], (Long) result[1]);
        }
        return map;
    }
    
    public Map<TipoImmobile, Long> getContImmobiliPerTipo() {
        logger.debug("Recupero conteggio immobili per tipo");
        List<Object[]> results = immobileRepository.countImmobiliPerTipo();
        Map<TipoImmobile, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put((TipoImmobile) result[0], (Long) result[1]);
        }
        return map;
    }
}
