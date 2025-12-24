package com.epicode.Progetto_Backend.controller;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.epicode.Progetto_Backend.dto.LocatarioRequestDTO;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.service.LocatarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * LocatarioController - Controller REST per la gestione dei locatari.
 * 
 * Gestisce le operazioni CRUD sui locatari:
 * - Visualizzazione locatari (lista paginata, per ID, locatario corrente)
 * - Creazione nuovi locatari
 * - Aggiornamento locatari esistenti
 * - Eliminazione locatari (solo ADMIN)
 * - Query speciali (locatari con contratti di lunga durata)
 * 
 * Ogni locatario è associato a un utente (User) tramite relazione One-to-One.
 * I LOCATARIO possono visualizzare solo i propri dati.
 * 
 * Autorizzazioni:
 * - ADMIN, MANAGER: Accesso completo (no DELETE per MANAGER)
 * - LOCATARIO: Può visualizzare solo i propri dati (GET /me)
 * 
 * @see com.epicode.Progetto_Backend.service.LocatarioService
 */
@RestController
@RequestMapping("/api/locatari")
@RequiredArgsConstructor
public class LocatarioController {
    
    private final LocatarioService locatarioService;
    
    /**
     * Ottiene tutti i locatari con paginazione.
     * 
     * @param pageable Parametri di paginazione (page, size, sort)
     * @return Pagina di locatari
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<Locatario>> getAllLocatari(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(locatarioService.getAllLocatari(pageable));
    }
    
    /**
     * Ottiene un locatario specifico per ID.
     * 
     * @param id ID del locatario da recuperare
     * @return Locatario completo con dati anagrafici e riferimenti
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Locatario> getLocatarioById(@PathVariable Long id) {
        return ResponseEntity.ok(locatarioService.getLocatarioById(id));
    }
    
    /**
     * Ottiene il locatario associato all'utente corrente autenticato.
     * 
     * Utilizzato dai LOCATARIO per visualizzare i propri dati anagrafici.
     * 
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Locatario dell'utente corrente
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('LOCATARIO')")
    public ResponseEntity<Locatario> getMyLocatario(Authentication authentication) {
        String email = authentication.getName();
        com.epicode.Progetto_Backend.entity.User user = locatarioService.getUserByEmail(email);
        return ResponseEntity.ok(locatarioService.getLocatarioByUserId(user.getId()));
    }
    
    /**
     * Crea un nuovo locatario.
     * 
     * Il locatario viene associato a un utente esistente tramite userId.
     * Il codice fiscale deve essere univoco.
     * 
     * @param request DTO con dati del locatario (nome, cognome, cf, indirizzo, telefono, userId)
     * @return Locatario creato con ID assegnato (201 Created)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Locatario> createLocatario(@Valid @RequestBody LocatarioRequestDTO request) {
        Locatario locatario = locatarioService.createLocatario(request);
        URI location = Objects.requireNonNull(URI.create("/api/locatari/" + locatario.getId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(locatario);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Locatario> updateLocatario(@PathVariable Long id, @Valid @RequestBody LocatarioRequestDTO request) {
        return ResponseEntity.ok(locatarioService.updateLocatario(id, request));
    }
    
    /**
     * Elimina un locatario dal sistema (solo ADMIN).
     * 
     * @param id ID del locatario da eliminare
     * @return 204 No Content se l'eliminazione è riuscita
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocatario(@PathVariable Long id) {
        locatarioService.deleteLocatario(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Ottiene i locatari che hanno contratti con durata maggiore di 5 anni.
     * 
     * Utile per identificare i clienti con contratti di lunga durata.
     * 
     * @return Lista di locatari con contratti lunghi
     */
    @GetMapping("/contratti-lunghi")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Locatario>> getLocatariConContrattiLunghi() {
        return ResponseEntity.ok(locatarioService.getLocatariConContrattiLunghiDurata());
    }
}
