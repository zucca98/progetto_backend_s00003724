package com.epicode.Progetto_Backend.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.ManutenzioneRequestDTO;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Manutenzione;
import com.epicode.Progetto_Backend.exception.EntityNotFoundException;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.ManutenzioneRepository;

/**
 * ManutenzioneService - Servizio per la gestione delle manutenzioni.
 * 
 * Gestisce tutte le operazioni CRUD sulle manutenzioni:
 * - Recupero manutenzioni (lista, per ID, per locatario, per immobile)
 * - Creazione manutenzioni con invio email di conferma (asincrono)
 * - Aggiornamento manutenzioni
 * - Eliminazione manutenzioni
 * - Query personalizzate (per anno, per importo, statistiche)
 * 
 * Caratteristiche:
 * - Alla creazione, invia email di conferma richiesta al locatario (asincrono)
 * - Fornisce metodi per recuperare manutenzioni tramite email utente (per LOCATARIO)
 * - Supporta statistiche aggregate (totale spese per anno e città)
 * 
 * Query personalizzate:
 * - getManutenzioniByLocatarioAndAnno: Manutenzioni di un locatario in un anno
 * - getDateManutenzioniByLocatarioAndImportoMaggiore: Date manutenzioni con importo > X
 * - getTotaleSpeseManutenzionePerAnnoCitta: Statistiche aggregate per anno e città
 * 
 * Utilizzato da:
 * - ManutenzioneController per gli endpoint REST
 * - MutationResolver per le mutation GraphQL
 * 
 * @see com.epicode.Progetto_Backend.entity.Manutenzione
 */
@Service
public class ManutenzioneService {
    
    private static final Logger logger = LoggerFactory.getLogger(ManutenzioneService.class);
    
    @Autowired
    private ManutenzioneRepository manutenzioneRepository;
    
    @Autowired
    private ImmobileRepository immobileRepository;
    
    @Autowired
    private LocatarioRepository locatarioRepository;
    
    @Autowired
    private MailgunService mailgunService;
    
    public List<Manutenzione> getAllManutenzioni() {
        logger.debug("Recupero di tutte le manutenzioni");
        List<Manutenzione> manutenzioni = manutenzioneRepository.findAll();
        logger.info("Recuperate {} manutenzioni", manutenzioni.size());
        return manutenzioni;
    }
    
    @SuppressWarnings("null")
    public Manutenzione getManutenzioneById(Long id) {
        logger.debug("Recupero manutenzione con ID: {}", id);
        return manutenzioneRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Manutenzione non trovata con ID: {}", id);
                    return new EntityNotFoundException("Manutenzione", id);
                });
    }
    
    public List<Manutenzione> getManutenzioniByLocatarioId(Long locatarioId) {
        logger.debug("Recupero manutenzioni per locatario ID: {}", locatarioId);
        return manutenzioneRepository.findByLocatarioId(locatarioId);
    }
    
    @SuppressWarnings("null")
    @Transactional
    public Manutenzione createManutenzione(ManutenzioneRequestDTO request) {
        logger.info("Creazione nuova manutenzione. Immobile ID: {}, Locatario ID: {}, Tipo: {}, Importo: {}", 
                request.getImmobileId(), request.getLocatarioId(), request.getTipo(), request.getImporto());
        
        Immobile immobile = immobileRepository.findById(request.getImmobileId())
                .orElseThrow(() -> {
                    logger.error("Immobile non trovato con ID: {}", request.getImmobileId());
                    return new EntityNotFoundException("Immobile", request.getImmobileId());
                });
        
        Locatario locatario = locatarioRepository.findById(request.getLocatarioId())
                .orElseThrow(() -> {
                    logger.error("Locatario non trovato con ID: {}", request.getLocatarioId());
                    return new EntityNotFoundException("Locatario", request.getLocatarioId());
                });
        
        Manutenzione manutenzione = Manutenzione.builder()
                .immobile(immobile)
                .locatario(locatario)
                .dataMan(request.getDataMan())
                .importo(request.getImporto())
                .tipo(request.getTipo())
                .descrizione(request.getDescrizione())
                .build();
        
        Manutenzione saved = manutenzioneRepository.save(manutenzione);
        logger.info("Manutenzione creata con successo. ID: {}", saved.getId());
        
        // Invia email di conferma richiesta manutenzione (asincrono)
        String locatarioName = locatario.getNome() + " " + locatario.getCognome();
        String locatarioEmail = locatario.getUser().getEmail();
        String immobileIndirizzo = immobile.getIndirizzo();
        mailgunService.sendMaintenanceRequestConfirmation(
            locatarioEmail,
            locatarioName,
            immobileIndirizzo,
            saved.getTipo(),
            saved.getDescrizione(),
            saved.getDataMan()
        );
        
        return saved;
    }
    
    @SuppressWarnings("null")
    @Transactional
    public Manutenzione updateManutenzione(Long id, ManutenzioneRequestDTO request) {
        logger.info("Aggiornamento manutenzione con ID: {}", id);
        Manutenzione manutenzione = getManutenzioneById(id);

        if (request.getImmobileId() != null) {
            Immobile immobile = immobileRepository.findById(request.getImmobileId())
                    .orElseThrow(() -> {
                        logger.error("Immobile non trovato con ID: {}", request.getImmobileId());
                        return new EntityNotFoundException("Immobile", request.getImmobileId());
                    });
            manutenzione.setImmobile(immobile);
        }

        if (request.getLocatarioId() != null) {
            Locatario locatario = locatarioRepository.findById(request.getLocatarioId())
                    .orElseThrow(() -> {
                        logger.error("Locatario non trovato con ID: {}", request.getLocatarioId());
                        return new EntityNotFoundException("Locatario", request.getLocatarioId());
                    });
            manutenzione.setLocatario(locatario);
        }

        if (request.getDataMan() != null) {
            manutenzione.setDataMan(request.getDataMan());
        }
        if (request.getImporto() != null) {
            manutenzione.setImporto(request.getImporto());
        }
        if (request.getTipo() != null) {
            manutenzione.setTipo(request.getTipo());
        }
        if (request.getDescrizione() != null) {
            manutenzione.setDescrizione(request.getDescrizione());
        }

        Manutenzione updated = manutenzioneRepository.save(manutenzione);
        logger.info("Manutenzione aggiornata con successo. ID: {}", id);
        return updated;
    }

    @SuppressWarnings("null")
    @Transactional
    public void deleteManutenzione(Long id) {
        logger.info("Eliminazione manutenzione con ID: {}", id);
        if (!manutenzioneRepository.existsById(id)) {
            logger.warn("Tentativo di eliminare manutenzione inesistente con ID: {}", id);
            throw new EntityNotFoundException("Manutenzione", id);
        }
        manutenzioneRepository.deleteById(id);
        logger.info("Manutenzione eliminata con successo. ID: {}", id);
    }

    // Query custom
    public List<Manutenzione> getManutenzioniByLocatarioAndAnno(Long locatarioId, int anno) {
        return manutenzioneRepository.findManutenzioniByLocatarioAndAnno(locatarioId, anno);
    }
    
    public List<LocalDate> getDateManutenzioniByLocatarioAndImportoMaggiore(Long locatarioId, Double importo) {
        return manutenzioneRepository.findDateManutenzioniByLocatarioAndImportoMaggiore(locatarioId, importo);
    }
    
    public Map<String, Map<String, Double>> getTotaleSpeseManutenzionePerAnnoCitta() {
        List<Object[]> results = manutenzioneRepository.findTotaleSpeseManutenzionePerAnnoCitta();
        Map<String, Map<String, Double>> map = new HashMap<>();

        for (Object[] result : results) {
            String anno = result[0].toString();
            String citta = (String) result[1];
            Double totale = (Double) result[2];

            map.computeIfAbsent(anno, k -> new HashMap<>()).put(citta, totale);
        }

        return map;
    }

    public List<Manutenzione> getManutenzioniByLocatarioEmail(String email) {
        return manutenzioneRepository.findByLocatarioUserEmail(email);
    }
}
