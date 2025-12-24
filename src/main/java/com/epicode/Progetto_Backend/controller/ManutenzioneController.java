package com.epicode.Progetto_Backend.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.epicode.Progetto_Backend.dto.ManutenzioneRequestDTO;
import com.epicode.Progetto_Backend.entity.Manutenzione;
import com.epicode.Progetto_Backend.service.ManutenzioneService;

import jakarta.validation.Valid;

/**
 * ManutenzioneController - Controller REST per la gestione delle manutenzioni.
 * 
 * Gestisce le operazioni CRUD sulle manutenzioni:
 * - Visualizzazione manutenzioni (tutte, per ID, del locatario corrente, per locatario/anno, per importo)
 * - Creazione nuove manutenzioni
 * - Aggiornamento manutenzioni esistenti
 * - Eliminazione manutenzioni (solo ADMIN)
 * - Statistiche manutenzioni (totale spese per anno e città)
 * 
 * Caratteristiche:
 * - Ogni manutenzione è associata a un immobile e un locatario
 * - Tipi di manutenzione: ORDINARIA, STRAORDINARIA
 * - I LOCATARIO possono visualizzare solo le proprie manutenzioni
 * - Controlli di accesso per prevenire Broken Access Control
 * 
 * Autorizzazioni:
 * - ADMIN, MANAGER: Accesso completo (no DELETE per MANAGER)
 * - LOCATARIO: Può visualizzare solo le proprie manutenzioni
 * 
 * @see com.epicode.Progetto_Backend.service.ManutenzioneService
 */
@RestController
@RequestMapping("/api/manutenzioni")
public class ManutenzioneController {
    
    @Autowired
    private ManutenzioneService manutenzioneService;
    
    /**
     * Ottiene tutte le manutenzioni del sistema.
     * 
     * @return Lista di tutte le manutenzioni
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Manutenzione>> getAllManutenzioni() {
        return ResponseEntity.ok(manutenzioneService.getAllManutenzioni());
    }
    
    /**
     * Ottiene tutte le manutenzioni del locatario corrente autenticato.
     * 
     * Utilizzato dai LOCATARIO per visualizzare le proprie manutenzioni.
     * 
     * NOTA: Questa route deve essere definita PRIMA della route generica /{id}
     * per evitare conflitti di routing (Spring mapperebbe /me come {id}).
     * 
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Lista di manutenzioni del locatario corrente
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('LOCATARIO')")
    public ResponseEntity<List<Manutenzione>> getMyManutenzioni(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(manutenzioneService.getManutenzioniByLocatarioEmail(email));
    }
    
    /**
     * Ottiene il totale delle spese di manutenzione raggruppate per anno e città.
     * 
     * Restituisce una struttura dati a due livelli:
     * - Primo livello: Anno (String)
     * - Secondo livello: Città (String) -> Totale spese (Double)
     * 
     * Esempio: {"2024": {"Milano": 15000.0, "Roma": 8000.0}, "2025": {...}}
     * 
     * @return Mappa annidata: anno -> (città -> totale spese)
     */
    @GetMapping("/totale-per-anno-citta")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Map<String, Double>>> getTotaleSpeseManutenzionePerAnnoCitta() {
        return ResponseEntity.ok(manutenzioneService.getTotaleSpeseManutenzionePerAnnoCitta());
    }
    
    /**
     * Ottiene tutte le manutenzioni di un locatario per un anno specifico.
     * 
     * @param locatarioId ID del locatario
     * @param anno Anno delle manutenzioni (es: 2024)
     * @param authentication Oggetto Spring Security (usato per debug)
     * @return Lista di manutenzioni del locatario per l'anno specificato
     */
    @GetMapping("/locatario/{locatarioId}/anno/{anno}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Manutenzione>> getManutenzioniByLocatarioAndAnno(
            @PathVariable Long locatarioId, 
            @PathVariable int anno,
            Authentication authentication) {
        // Debug: verifica autenticazione e ruoli
        if (authentication != null) {
            System.out.println("User: " + authentication.getName());
            System.out.println("Authorities: " + authentication.getAuthorities());
        }
        return ResponseEntity.ok(manutenzioneService.getManutenzioniByLocatarioAndAnno(locatarioId, anno));
    }
    
    /**
     * Ottiene le date delle manutenzioni di un locatario con importo maggiore di un valore specifico.
     * 
     * Utile per identificare le manutenzioni più costose di un locatario.
     * 
     * @param locatarioId ID del locatario
     * @param importo Importo minimo (es: 1000.0)
     * @return Lista di date (LocalDate) delle manutenzioni che superano l'importo
     */
    @GetMapping("/locatario/{locatarioId}/importo-maggiore/{importo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LocalDate>> getDateManutenzioniByImporto(
            @PathVariable Long locatarioId, @PathVariable Double importo) {
        return ResponseEntity.ok(manutenzioneService.getDateManutenzioniByLocatarioAndImportoMaggiore(locatarioId, importo));
    }
    
    /**
     * Ottiene una manutenzione specifica per ID.
     * 
     * Per i LOCATARIO, verifica che la manutenzione appartenga al locatario corrente
     * per prevenire accessi non autorizzati a manutenzioni di altri locatari.
     * 
     * @param id ID della manutenzione da recuperare
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Manutenzione completa con riferimento a immobile e locatario
     * @throws AccessDeniedException se un LOCATARIO tenta di accedere a una manutenzione non propria
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'LOCATARIO')")
    public ResponseEntity<Manutenzione> getManutenzioneById(
            @PathVariable Long id,
            Authentication authentication) {
        Manutenzione manutenzione = manutenzioneService.getManutenzioneById(id);
        
        // Verifica che se è LOCATARIO, la manutenzione appartenga al locatario corrente
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LOCATARIO"))) {
            String email = authentication.getName();
            List<Manutenzione> myManutenzioni = manutenzioneService.getManutenzioniByLocatarioEmail(email);
            boolean isMyManutenzione = myManutenzioni.stream()
                    .anyMatch(m -> m.getId().equals(id));
            if (!isMyManutenzione) {
                throw new AccessDeniedException("Non hai accesso a questa manutenzione");
            }
        }
        
        return ResponseEntity.ok(manutenzione);
    }
    
    /**
     * Crea una nuova manutenzione.
     * 
     * @param request DTO con dati della manutenzione (immobileId, locatarioId, dataMan, importo, tipo, descrizione)
     * @return Manutenzione creata con ID assegnato (201 Created)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Manutenzione> createManutenzione(@Valid @RequestBody ManutenzioneRequestDTO request) {
        Manutenzione manutenzione = manutenzioneService.createManutenzione(request);
        URI location = Objects.requireNonNull(URI.create("/api/manutenzioni/" + manutenzione.getId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(manutenzione);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Manutenzione> updateManutenzione(
            @PathVariable Long id,
            @Valid @RequestBody ManutenzioneRequestDTO request) {
        return ResponseEntity.ok(manutenzioneService.updateManutenzione(id, request));
    }

    /**
     * Elimina una manutenzione dal sistema (solo ADMIN).
     * 
     * @param id ID della manutenzione da eliminare
     * @return 204 No Content se l'eliminazione è riuscita
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteManutenzione(@PathVariable Long id) {
        manutenzioneService.deleteManutenzione(id);
        return ResponseEntity.noContent().build();
    }
}
