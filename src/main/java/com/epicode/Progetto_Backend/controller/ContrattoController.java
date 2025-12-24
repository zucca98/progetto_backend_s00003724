package com.epicode.Progetto_Backend.controller;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epicode.Progetto_Backend.dto.ContrattoRequestDTO;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.service.ContrattoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * ContrattoController - Controller REST per la gestione dei contratti di affitto.
 * 
 * Gestisce le operazioni CRUD sui contratti:
 * - Visualizzazione contratti (lista paginata, per ID, contratti del locatario corrente)
 * - Creazione nuovi contratti (con generazione automatica delle rate)
 * - Aggiornamento contratti esistenti
 * - Eliminazione contratti (solo ADMIN)
 * - Query speciali (contratti con rate non pagate)
 * 
 * Caratteristiche:
 * - Alla creazione di un contratto, le rate vengono generate automaticamente
 *   in base alla frequenza (MENSILE, TRIMESTRALE, etc.) e durata
 * - I LOCATARIO possono accedere solo ai propri contratti
 * - Controlli di accesso per prevenire Broken Access Control
 * 
 * Autorizzazioni:
 * - ADMIN, MANAGER: Accesso completo (no DELETE per MANAGER)
 * - LOCATARIO: Può visualizzare solo i propri contratti
 * 
 * @see com.epicode.Progetto_Backend.service.ContrattoService
 */
@RestController
@RequestMapping("/api/contratti")
@RequiredArgsConstructor
public class ContrattoController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContrattoController.class);
    
    private final ContrattoService contrattoService;
    
    /**
     * Ottiene tutti i contratti con paginazione.
     * 
     * @param pageable Parametri di paginazione (page, size, sort)
     * @return Pagina di contratti
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<Contratto>> getAllContratti(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        logger.info("Fetching all contratti (paginated)");
        return ResponseEntity.ok(contrattoService.getAllContratti(pageable));
    }
    
    /**
     * Ottiene un contratto specifico per ID.
     * 
     * Per i LOCATARIO, verifica che il contratto appartenga al locatario corrente
     * per prevenire accessi non autorizzati a contratti di altri locatari.
     * 
     * @param id ID del contratto da recuperare
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Contratto completo con locatario, immobile e rate
     * @throws AccessDeniedException se un LOCATARIO tenta di accedere a un contratto non proprio
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'LOCATARIO')")
    public ResponseEntity<Contratto> getContrattoById(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Fetching contratto with id: {}", id);
        Contratto contratto = contrattoService.getContrattoById(id);
        
        // Verifica che se è LOCATARIO, il contratto appartenga al locatario corrente
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LOCATARIO"))) {
            String email = authentication.getName();
            List<Contratto> myContratti = contrattoService.getContrattiByLocatarioEmail(email);
            boolean isMyContratto = myContratti.stream()
                    .anyMatch(c -> c.getId().equals(id));
            if (!isMyContratto) {
                logger.warn("LOCATARIO {} ha tentato di accedere al contratto {} di un altro locatario", email, id);
                throw new AccessDeniedException("Non hai accesso a questo contratto");
            }
        }
        
        return ResponseEntity.ok(contratto);
    }
    
    /**
     * Ottiene tutti i contratti del locatario corrente autenticato.
     * 
     * Utilizzato dai LOCATARIO per visualizzare i propri contratti.
     * 
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Lista di contratti del locatario corrente
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('LOCATARIO')")
    public ResponseEntity<List<Contratto>> getMyContratti(Authentication authentication) {
        String email = authentication.getName();
        logger.info("Fetching contratti for locatario: {}", email);
        return ResponseEntity.ok(contrattoService.getContrattiByLocatarioEmail(email));
    }
    
    /**
     * Crea un nuovo contratto di affitto.
     * 
     * Alla creazione, il sistema genera automaticamente tutte le rate
     * in base alla frequenza (MENSILE, TRIMESTRALE, etc.) e durata del contratto.
     * 
     * Esempio: Contratto di 3 anni con frequenza TRIMESTRALE genera 12 rate.
     * 
     * @param request DTO con dati del contratto (locatarioId, immobileId, dataInizio, durataAnni, canoneAnnuo, frequenzaRata)
     * @return Contratto creato con ID e rate generate (201 Created)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Contratto> createContratto(@Valid @RequestBody ContrattoRequestDTO request) {
        logger.info("Creating new contratto");
        Contratto contratto = contrattoService.createContratto(request);
        logger.info("Contratto created with id: {}", contratto.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(Objects.requireNonNull(URI.create("/api/contratti/" + contratto.getId())))
                .body(contratto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Contratto> updateContratto(
            @PathVariable Long id,
            @Valid @RequestBody ContrattoRequestDTO request) {
        logger.info("Updating contratto with id: {}", id);
        return ResponseEntity.ok(contrattoService.updateContratto(id, request));
    }

    /**
     * Elimina un contratto dal sistema (solo ADMIN).
     * 
     * @param id ID del contratto da eliminare
     * @return 204 No Content se l'eliminazione è riuscita
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContratto(@PathVariable Long id) {
        logger.info("Deleting contratto with id: {}", id);
        contrattoService.deleteContratto(id);
        logger.info("Contratto with id: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Ottiene i contratti che hanno almeno tre rate non pagate.
     * 
     * Utile per identificare i contratti con problemi di morosità.
     * 
     * @return Lista di contratti con almeno 3 rate non pagate
     */
    @GetMapping("/rate-non-pagate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Contratto>> getContrattiConRateNonPagate() {
        logger.info("Fetching contratti with at least three unpaid rate");
        return ResponseEntity.ok(contrattoService.getContrattiConAlmenoTreRateNonPagate());
    }
}
