package com.epicode.Progetto_Backend.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.ContrattoRequestDTO;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.exception.EntityNotFoundException;
import com.epicode.Progetto_Backend.repository.ContrattoRepository;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RataRepository;

import lombok.RequiredArgsConstructor;

/**
 * ContrattoService - Servizio per la gestione dei contratti di affitto.
 * 
 * Gestisce tutte le operazioni CRUD sui contratti:
 * - Recupero contratti (lista paginata, per ID, per locatario)
 * - Creazione contratti con generazione automatica delle rate
 * - Aggiornamento contratti
 * - Eliminazione contratti (con eliminazione rate associate)
 * - Query personalizzate (contratti con morosità)
 * 
 * Caratteristiche principali:
 * - Alla creazione, genera automaticamente tutte le rate in base a frequenza e durata
 * - Calcola importo rata = canoneAnnuo / numero rate all'anno
 * - Invia notifica email al locatario dopo la creazione (asincrono)
 * - All'eliminazione, elimina manualmente le rate associate
 * 
 * Generazione rate automatica:
 * - MENSILE: durataAnni × 12 rate
 * - BIMESTRALE: durataAnni × 6 rate
 * - TRIMESTRALE: durataAnni × 4 rate (default)
 * - SEMESTRALE: durataAnni × 2 rate
 * - ANNUALE: durataAnni rate
 * 
 * Utilizzato da:
 * - ContrattoController per gli endpoint REST
 * - MutationResolver per le mutation GraphQL
 * 
 * @see com.epicode.Progetto_Backend.entity.Contratto
 * @see com.epicode.Progetto_Backend.entity.Rata
 * @see com.epicode.Progetto_Backend.entity.FrequenzaRata
 */
@Service
@RequiredArgsConstructor
public class ContrattoService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContrattoService.class);
    
    private final ContrattoRepository contrattoRepository;
    private final LocatarioRepository locatarioRepository;
    private final ImmobileRepository immobileRepository;
    private final RataRepository rataRepository;
    private final MailgunService mailgunService;
    
    public Page<Contratto> getAllContratti(Pageable pageable) {
        logger.debug("Recupero contratti paginati. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Contratto> page = contrattoRepository.findAll(pageable);
        logger.info("Recuperati {} contratti su {} totali", page.getNumberOfElements(), page.getTotalElements());
        return page;
    }
    
    @Deprecated
    public List<Contratto> getAllContratti() {
        logger.debug("Recupero di tutti i contratti (deprecated - use paginated version)");
        List<Contratto> contratti = contrattoRepository.findAll();
        logger.info("Recuperati {} contratti", contratti.size());
        return contratti;
    }
    
    @SuppressWarnings("null")
    public Contratto getContrattoById(Long id) {
        logger.debug("Recupero contratto con ID: {}", id);
        return contrattoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Contratto non trovato con ID: {}", id);
                    return new EntityNotFoundException("Contratto", id);
                });
    }
    
    public List<Contratto> getContrattiByLocatarioId(Long locatarioId) {
        logger.debug("Recupero contratti per locatario ID: {}", locatarioId);
        return contrattoRepository.findByLocatarioId(locatarioId);
    }
    
    public List<Contratto> getContrattiByLocatarioEmail(String email) {
        logger.debug("Recupero contratti per locatario email: {}", email);
        return contrattoRepository.findByLocatarioUserEmail(email);
    }
    
    @SuppressWarnings("null")
    @Transactional
    public Contratto createContratto(ContrattoRequestDTO request) {
        logger.info("Creazione nuovo contratto per locatario ID: {}, immobile ID: {}", 
                request.getLocatarioId(), request.getImmobileId());
        
        Locatario locatario = locatarioRepository.findById(request.getLocatarioId())
                .orElseThrow(() -> {
                    logger.error("Locatario non trovato con ID: {}", request.getLocatarioId());
                    return new EntityNotFoundException("Locatario", request.getLocatarioId());
                });
        
        Immobile immobile = immobileRepository.findById(request.getImmobileId())
                .orElseThrow(() -> {
                    logger.error("Immobile non trovato con ID: {}", request.getImmobileId());
                    return new EntityNotFoundException("Immobile", request.getImmobileId());
                });
        
        Contratto contratto = Contratto.builder()
                .locatario(locatario)
                .immobile(immobile)
                .dataInizio(request.getDataInizio())
                .durataAnni(request.getDurataAnni())
                .canoneAnnuo(request.getCanoneAnnuo())
                .frequenzaRata(request.getFrequenzaRata())
                .build();
        
        Contratto savedContratto = contrattoRepository.save(contratto);
        logger.info("Contratto creato con successo. ID: {}", savedContratto.getId());
        
        // Genera automaticamente le rate
        generaRate(savedContratto);
        logger.debug("Rate generate per contratto ID: {}", savedContratto.getId());
        
        // Invia notifica email al locatario (asincrono)
        String locatarioName = locatario.getNome() + " " + locatario.getCognome();
        String locatarioEmail = locatario.getUser().getEmail();
        String immobileIndirizzo = immobile.getIndirizzo();
        mailgunService.sendContractNotification(locatarioEmail, locatarioName, immobileIndirizzo);
        
        return savedContratto;
    }
    
    /**
     * Genera automaticamente tutte le rate per un contratto.
     * 
     * Calcola il numero di rate in base alla frequenza e durata:
     * - MENSILE: durataAnni × 12
     * - BIMESTRALE: durataAnni × 6
     * - TRIMESTRALE: durataAnni × 4
     * - SEMESTRALE: durataAnni × 2
     * - ANNUALE: durataAnni
     * 
     * L'importo di ogni rata è calcolato come: canoneAnnuo / numero rate all'anno.
     * Le date di scadenza vengono calcolate incrementando la data inizio
     * in base alla frequenza.
     * 
     * @param contratto Contratto per cui generare le rate
     */
    @SuppressWarnings("null")
    private void generaRate(Contratto contratto) {
        int numeroRate = switch (contratto.getFrequenzaRata()) {
            case MENSILE -> contratto.getDurataAnni() * 12;
            case BIMESTRALE -> contratto.getDurataAnni() * 6;
            case TRIMESTRALE -> contratto.getDurataAnni() * 4;
            case SEMESTRALE -> contratto.getDurataAnni() * 2;
            case ANNUALE -> contratto.getDurataAnni();
        };
        
        double importoRata = contratto.getCanoneAnnuo() / numeroRate;
        
        LocalDate dataScadenza = contratto.getDataInizio();
        for (int i = 1; i <= numeroRate; i++) {
            Rata rata = Rata.builder()
                    .contratto(contratto)
                    .numeroRata(i)
                    .dataScadenza(dataScadenza)
                    .importo(importoRata)
                    .pagata('N')
                    .build();
            
            Rata savedRata = rataRepository.save(rata);
            contratto.getRate().add(savedRata);
            
            // Calcola prossima scadenza
            dataScadenza = switch (contratto.getFrequenzaRata()) {
                case MENSILE -> dataScadenza.plusMonths(1);
                case BIMESTRALE -> dataScadenza.plusMonths(2);
                case TRIMESTRALE -> dataScadenza.plusMonths(3);
                case SEMESTRALE -> dataScadenza.plusMonths(6);
                case ANNUALE -> dataScadenza.plusYears(1);
            };
        }
    }
    
    @SuppressWarnings("null")
    @Transactional
    public Contratto updateContratto(Long id, ContrattoRequestDTO request) {
        logger.info("Aggiornamento contratto con ID: {}", id);
        Contratto contratto = getContrattoById(id);

        if (request.getLocatarioId() != null) {
            Locatario locatario = locatarioRepository.findById(request.getLocatarioId())
                    .orElseThrow(() -> {
                        logger.error("Locatario non trovato con ID: {}", request.getLocatarioId());
                        return new EntityNotFoundException("Locatario", request.getLocatarioId());
                    });
            contratto.setLocatario(locatario);
            logger.debug("Locatario aggiornato per contratto ID: {}", id);
        }

        if (request.getImmobileId() != null) {
            Immobile immobile = immobileRepository.findById(request.getImmobileId())
                    .orElseThrow(() -> {
                        logger.error("Immobile non trovato con ID: {}", request.getImmobileId());
                        return new EntityNotFoundException("Immobile", request.getImmobileId());
                    });
            contratto.setImmobile(immobile);
            logger.debug("Immobile aggiornato per contratto ID: {}", id);
        }

        if (request.getDataInizio() != null) {
            contratto.setDataInizio(request.getDataInizio());
        }
        if (request.getDurataAnni() != null) {
            contratto.setDurataAnni(request.getDurataAnni());
        }
        if (request.getCanoneAnnuo() != null) {
            contratto.setCanoneAnnuo(request.getCanoneAnnuo());
        }
        if (request.getFrequenzaRata() != null) {
            contratto.setFrequenzaRata(request.getFrequenzaRata());
        }

        Contratto updated = contrattoRepository.save(contratto);
        logger.info("Contratto aggiornato con successo. ID: {}", id);
        return updated;
    }

    @SuppressWarnings("null")
    @Transactional
    public void deleteContratto(Long id) {
        logger.info("Eliminazione contratto con ID: {}", id);
        Contratto contratto = contrattoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Tentativo di eliminare contratto inesistente con ID: {}", id);
                    return new EntityNotFoundException("Contratto", id);
                });
        
        // Elimina manualmente le rate associate prima di eliminare il contratto
        List<Rata> rate = rataRepository.findByContrattoId(id);
        if (!rate.isEmpty()) {
            logger.debug("Eliminazione di {} rate associate al contratto ID: {}", rate.size(), id);
            rataRepository.deleteAll(rate);
        }
        
        contrattoRepository.delete(contratto);
        logger.info("Contratto eliminato con successo. ID: {}", id);
    }

    // Query custom
    public List<Contratto> getContrattiConAlmenoTreRateNonPagate() {
        logger.debug("Recupero contratti con almeno tre rate non pagate");
        return contrattoRepository.findContrattiConAlmenoTreRateNonPagate();
    }
}
