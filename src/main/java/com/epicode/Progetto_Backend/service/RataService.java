package com.epicode.Progetto_Backend.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.RataRequestDTO;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.exception.EntityNotFoundException;
import com.epicode.Progetto_Backend.repository.ContrattoRepository;
import com.epicode.Progetto_Backend.repository.RataRepository;

/**
 * RataService - Servizio per la gestione delle rate di affitto.
 * 
 * Gestisce tutte le operazioni CRUD sulle rate:
 * - Recupero rate (lista, per ID, per contratto, per locatario)
 * - Creazione rate (generalmente automatica, ma supporta creazione manuale)
 * - Aggiornamento rate (incluso stato pagamento)
 * - Eliminazione rate
 * - Query personalizzate (rate non pagate, rate scadute)
 * 
 * Caratteristiche:
 * - Le rate vengono generalmente generate automaticamente alla creazione di un contratto
 * - L'aggiornamento dello stato pagamento può inviare email di conferma (asincrono)
 * - Fornisce metodi per recuperare rate tramite email utente (per LOCATARIO)
 * 
 * Utilizzato da:
 * - RataController per gli endpoint REST
 * - MutationResolver per le mutation GraphQL
 * - ContrattoService per la generazione automatica delle rate
 * 
 * @see com.epicode.Progetto_Backend.entity.Rata
 * @see com.epicode.Progetto_Backend.service.ContrattoService
 */
@Service
public class RataService {
    
    private static final Logger logger = LoggerFactory.getLogger(RataService.class);
    
    @Autowired
    private RataRepository rataRepository;
    
    @Autowired
    private ContrattoRepository contrattoRepository;
    
    @Autowired
    private MailgunService mailgunService;
    
    public List<Rata> getAllRate() {
        logger.debug("Recupero di tutte le rate");
        List<Rata> rate = rataRepository.findAll();
        logger.info("Recuperate {} rate", rate.size());
        return rate;
    }
    
    @SuppressWarnings("null")
    public Rata getRataById(Long id) {
        logger.debug("Recupero rata con ID: {}", id);
        return rataRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Rata non trovata con ID: {}", id);
                    return new EntityNotFoundException("Rata", id);
                });
    }
    
    public List<Rata> getRateByContrattoId(Long contrattoId) {
        logger.debug("Recupero rate per contratto ID: {}", contrattoId);
        return rataRepository.findByContrattoId(contrattoId);
    }
    
    @Transactional
    public Rata updateRataPagata(Long id, Character pagata) {
        logger.info("Aggiornamento stato pagamento rata ID: {} a {}", id, pagata);
        Rata rata = getRataById(id);
        Character vecchioStato = rata.getPagata();
        rata.setPagata(pagata);
        Rata updated = rataRepository.save(rata);
        logger.info("Stato pagamento rata aggiornato. ID: {}, Pagata: {}", id, pagata);
        
        // Invia conferma pagamento se la rata è stata appena pagata
        if (pagata == 'S' && vecchioStato == 'N') {
            sendPaymentConfirmationForRata(updated);
        }
        
        return updated;
    }
    
    /**
     * Invia email di conferma pagamento per una rata.
     * 
     * Viene chiamato automaticamente quando una rata viene marcata come pagata
     * (passaggio da 'N' a 'S'). L'invio è asincrono e non blocca l'operazione.
     * 
     * @param rata Rata per cui inviare la conferma
     */
    private void sendPaymentConfirmationForRata(Rata rata) {
        try {
            var contratto = rata.getContratto();
            var locatario = contratto.getLocatario();
            var immobile = contratto.getImmobile();
            
            String locatarioName = locatario.getNome() + " " + locatario.getCognome();
            String locatarioEmail = locatario.getUser().getEmail();
            String immobileIndirizzo = immobile.getIndirizzo();
            
            mailgunService.sendPaymentConfirmationEmail(
                locatarioEmail,
                locatarioName,
                rata.getNumeroRata(),
                rata.getImporto(),
                immobileIndirizzo
            );
        } catch (Exception e) {
            logger.error("Errore nell'invio conferma pagamento per rata ID {}: {}", rata.getId(), e.getMessage());
        }
    }
    
    public List<Rata> getRateNonPagate() {
        logger.debug("Recupero rate non pagate");
        return rataRepository.findByPagata('N');
    }
    
    public List<Rata> getRateScaduteNonPagate() {
        logger.debug("Recupero rate scadute e non pagate");
        return rataRepository.findRateScaduteNonPagate(LocalDate.now());
    }

    public List<Rata> getRateByLocatarioId(Long locatarioId) {
        return rataRepository.findByLocatarioId(locatarioId);
    }

    public List<Rata> getRateByLocatarioEmail(String email) {
        return rataRepository.findByLocatarioUserEmail(email);
    }

    @SuppressWarnings("null")
    @Transactional
    public Rata createRata(RataRequestDTO request) {
        logger.info("Creazione nuova rata. Contratto ID: {}, Numero: {}, Importo: {}", 
                request.getContrattoId(), request.getNumeroRata(), request.getImporto());
        
        Contratto contratto = contrattoRepository.findById(request.getContrattoId())
                .orElseThrow(() -> {
                    logger.error("Contratto non trovato con ID: {}", request.getContrattoId());
                    return new EntityNotFoundException("Contratto", request.getContrattoId());
                });
        
        Rata rata = Rata.builder()
                .contratto(contratto)
                .numeroRata(request.getNumeroRata())
                .dataScadenza(request.getDataScadenza())
                .importo(request.getImporto())
                .pagata(request.getPagata() != null ? request.getPagata() : 'N')
                .build();
        
        Rata saved = rataRepository.save(rata);
        logger.info("Rata creata con successo. ID: {}", saved.getId());
        return saved;
    }

    @SuppressWarnings("null")
    @Transactional
    public Rata updateRata(Long id, RataRequestDTO request) {
        logger.info("Aggiornamento rata con ID: {}", id);
        Rata rata = getRataById(id);
        
        if (request.getContrattoId() != null) {
            Contratto contratto = contrattoRepository.findById(request.getContrattoId())
                    .orElseThrow(() -> {
                        logger.error("Contratto non trovato con ID: {}", request.getContrattoId());
                        return new EntityNotFoundException("Contratto", request.getContrattoId());
                    });
            rata.setContratto(contratto);
        }
        
        if (request.getNumeroRata() != null) {
            rata.setNumeroRata(request.getNumeroRata());
        }
        if (request.getDataScadenza() != null) {
            rata.setDataScadenza(request.getDataScadenza());
        }
        if (request.getImporto() != null) {
            rata.setImporto(request.getImporto());
        }
        if (request.getPagata() != null) {
            rata.setPagata(request.getPagata());
        }
        
        Rata updated = rataRepository.save(rata);
        logger.info("Rata aggiornata con successo. ID: {}", id);
        return updated;
    }

    @SuppressWarnings("null")
    @Transactional
    public void deleteRata(Long id) {
        logger.info("Eliminazione rata con ID: {}", id);
        if (!rataRepository.existsById(id)) {
            logger.warn("Tentativo di eliminare rata inesistente con ID: {}", id);
            throw new EntityNotFoundException("Rata", id);
        }
        rataRepository.deleteById(id);
        logger.info("Rata eliminata con successo. ID: {}", id);
    }
}
