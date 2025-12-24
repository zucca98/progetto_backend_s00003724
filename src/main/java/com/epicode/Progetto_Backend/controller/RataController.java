package com.epicode.Progetto_Backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.service.ContrattoService;
import com.epicode.Progetto_Backend.service.RataService;

/**
 * RataController - Controller REST per la gestione delle rate di affitto.
 * 
 * Gestisce le operazioni sulle rate:
 * - Visualizzazione rate (tutte, per ID, per contratto, del locatario corrente)
 * - Aggiornamento stato pagamento delle rate
 * - Query speciali (rate non pagate, rate scadute e non pagate)
 * 
 * Caratteristiche:
 * - Le rate vengono generate automaticamente alla creazione di un contratto
 * - I LOCATARIO possono visualizzare solo le proprie rate
 * - Controlli di accesso per prevenire Broken Access Control
 * - Gli ADMIN/MANAGER possono marcare le rate come pagate
 * 
 * Autorizzazioni:
 * - ADMIN, MANAGER: Accesso completo a tutte le rate
 * - LOCATARIO: Può visualizzare solo le proprie rate
 * 
 * @see com.epicode.Progetto_Backend.service.RataService
 * @see com.epicode.Progetto_Backend.service.ContrattoService
 */
@RestController
@RequestMapping("/api/rate")
public class RataController {
    
    @Autowired
    private RataService rataService;
    
    @Autowired
    private ContrattoService contrattoService;
    
    /**
     * Ottiene tutte le rate del sistema.
     * 
     * @return Lista di tutte le rate
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Rata>> getAllRate() {
        return ResponseEntity.ok(rataService.getAllRate());
    }
    
    /**
     * Ottiene una rata specifica per ID.
     * 
     * Per i LOCATARIO, verifica che la rata appartenga al locatario corrente
     * per prevenire accessi non autorizzati a rate di altri locatari.
     * 
     * @param id ID della rata da recuperare
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Rata completa con riferimento al contratto
     * @throws AccessDeniedException se un LOCATARIO tenta di accedere a una rata non propria
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'LOCATARIO')")
    public ResponseEntity<Rata> getRataById(
            @PathVariable Long id,
            Authentication authentication) {
        Rata rata = rataService.getRataById(id);
        
        // Verifica che se è LOCATARIO, la rata appartenga al locatario corrente
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LOCATARIO"))) {
            String email = authentication.getName();
            List<Rata> myRate = rataService.getRateByLocatarioEmail(email);
            boolean isMyRata = myRate.stream()
                    .anyMatch(r -> r.getId().equals(id));
            if (!isMyRata) {
                throw new AccessDeniedException("Non hai accesso a questa rata");
            }
        }
        
        return ResponseEntity.ok(rata);
    }
    
    /**
     * Ottiene tutte le rate di un contratto specifico.
     * 
     * Per i LOCATARIO, verifica che il contratto appartenga al locatario corrente
     * per prevenire accessi non autorizzati alle rate di contratti di altri locatari.
     * 
     * @param contrattoId ID del contratto
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Lista di rate del contratto specificato
     * @throws AccessDeniedException se un LOCATARIO tenta di accedere a rate di un contratto non proprio
     */
    @GetMapping("/contratto/{contrattoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'LOCATARIO')")
    public ResponseEntity<List<Rata>> getRateByContratto(
            @PathVariable Long contrattoId,
            Authentication authentication) {
        
        // Verifica che se è LOCATARIO, il contratto appartenga al locatario corrente
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LOCATARIO"))) {
            String email = authentication.getName();
            List<Contratto> myContratti = contrattoService.getContrattiByLocatarioEmail(email);
            boolean isMyContratto = myContratti.stream()
                    .anyMatch(c -> c.getId().equals(contrattoId));
            if (!isMyContratto) {
                throw new AccessDeniedException("Non hai accesso alle rate di questo contratto");
            }
        }
        
        return ResponseEntity.ok(rataService.getRateByContrattoId(contrattoId));
    }
    
    /**
     * Aggiorna lo stato di pagamento di una rata.
     * 
     * @param id ID della rata da aggiornare
     * @param pagata Stato pagamento: 'S' (pagata) o 'N' (non pagata)
     * @return Rata aggiornata con nuovo stato pagamento
     */
    @PutMapping("/{id}/pagata")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Rata> updateRataPagata(@PathVariable Long id, @RequestParam Character pagata) {
        return ResponseEntity.ok(rataService.updateRataPagata(id, pagata));
    }
    
    /**
     * Ottiene tutte le rate non pagate del sistema.
     * 
     * Utile per monitorare le rate in attesa di pagamento.
     * 
     * @return Lista di rate con pagata = 'N'
     */
    @GetMapping("/non-pagate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Rata>> getRateNonPagate() {
        return ResponseEntity.ok(rataService.getRateNonPagate());
    }
    
    /**
     * Ottiene tutte le rate scadute e non ancora pagate.
     * 
     * Utile per identificare le situazioni di morosità.
     * 
     * @return Lista di rate con dataScadenza < oggi e pagata = 'N'
     */
    @GetMapping("/scadute")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Rata>> getRateScaduteNonPagate() {
        return ResponseEntity.ok(rataService.getRateScaduteNonPagate());
    }

    /**
     * Ottiene tutte le rate del locatario corrente autenticato.
     * 
     * Utilizzato dai LOCATARIO per visualizzare le proprie rate.
     * 
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Lista di rate del locatario corrente
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('LOCATARIO')")
    public ResponseEntity<List<Rata>> getMyRate(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(rataService.getRateByLocatarioEmail(email));
    }
}
