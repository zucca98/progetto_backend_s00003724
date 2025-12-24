package com.epicode.Progetto_Backend.controller;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epicode.Progetto_Backend.dto.ImmobileRequestDTO;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.service.ImmobileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * ImmobileController - Controller REST per la gestione degli immobili.
 * 
 * Gestisce le operazioni CRUD sugli immobili (appartamenti, negozi, uffici):
 * - Visualizzazione immobili (lista paginata, per ID)
 * - Creazione nuovi immobili (con tipo specifico: APPARTAMENTO, NEGOZIO, UFFICIO)
 * - Aggiornamento immobili esistenti
 * - Eliminazione immobili (solo ADMIN)
 * - Statistiche immobili (per città, per tipo)
 * 
 * Tipi di immobili supportati:
 * - APPARTAMENTO: Richiede piano e numCamere
 * - NEGOZIO: Richiede vetrine e magazzinoMq
 * - UFFICIO: Richiede postiLavoro e saleRiunioni
 * 
 * Autorizzazioni:
 * - ADMIN: Accesso completo (incluso DELETE)
 * - MANAGER: Può creare, leggere e aggiornare (no DELETE)
 * - LOCATARIO: Può solo visualizzare gli immobili
 * 
 * @see com.epicode.Progetto_Backend.service.ImmobileService
 */
@RestController
@RequestMapping("/api/immobili")
@RequiredArgsConstructor
public class ImmobileController {
    
    private final ImmobileService immobileService;
    
    /**
     * Ottiene tutti gli immobili con paginazione.
     * 
     * @param pageable Parametri di paginazione (page, size, sort)
     * @return Pagina di immobili
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'LOCATARIO')")
    public ResponseEntity<Page<Immobile>> getAllImmobili(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(immobileService.getAllImmobili(pageable));
    }
    
    /**
     * Ottiene un immobile specifico per ID.
     * 
     * @param id ID dell'immobile da recuperare
     * @return Immobile completo con tutti i dettagli specifici del tipo
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'LOCATARIO')")
    public ResponseEntity<Immobile> getImmobileById(@PathVariable Long id) {
        return ResponseEntity.ok(immobileService.getImmobileById(id));
    }
    
    /**
     * Crea un nuovo immobile.
     * 
     * Il tipo di immobile determina quali campi sono richiesti:
     * - APPARTAMENTO: piano, numCamere
     * - NEGOZIO: vetrine, magazzinoMq
     * - UFFICIO: postiLavoro, saleRiunioni
     * 
     * @param request DTO con i dati dell'immobile da creare
     * @return Immobile creato con ID assegnato (201 Created)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Immobile> createImmobile(@Valid @RequestBody ImmobileRequestDTO request) {
        Immobile immobile = immobileService.createImmobile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(Objects.requireNonNull(URI.create("/api/immobili/" + immobile.getId())))
                .body(immobile);
    }

    /**
     * Aggiorna un immobile esistente.
     * 
     * @param id ID dell'immobile da aggiornare
     * @param request DTO con i dati aggiornati
     * @return Immobile aggiornato
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Immobile> updateImmobile(
            @PathVariable Long id,
            @Valid @RequestBody ImmobileRequestDTO request) {
        return ResponseEntity.ok(immobileService.updateImmobile(id, request));
    }

    /**
     * Elimina un immobile dal sistema (solo ADMIN).
     * 
     * @param id ID dell'immobile da eliminare
     * @return 204 No Content se l'eliminazione è riuscita
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImmobile(@PathVariable Long id) {
        immobileService.deleteImmobile(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Ottiene statistiche sugli immobili affittati raggruppati per città.
     * 
     * Restituisce una mappa con città come chiave e numero di immobili affittati come valore.
     * Solo gli immobili con almeno un contratto attivo vengono conteggiati.
     * 
     * @return Mappa città -> numero immobili affittati
     */
    @GetMapping("/per-citta")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Long>> getImmobiliAffittatiPerCitta() {
        return ResponseEntity.ok(immobileService.getImmobiliAffittatiPerCitta());
    }
    
    /**
     * Ottiene il conteggio degli immobili raggruppati per tipo.
     * 
     * Restituisce una mappa con il tipo di immobile come chiave e il numero totale come valore.
     * 
     * @return Mappa tipo immobile -> conteggio totale
     */
    @GetMapping("/per-tipo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<TipoImmobile, Long>> getContImmobiliPerTipo() {
        return ResponseEntity.ok(immobileService.getContImmobiliPerTipo());
    }
}
