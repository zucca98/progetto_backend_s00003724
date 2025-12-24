package com.epicode.Progetto_Backend.graphql;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Manutenzione;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.service.ContrattoService;
import com.epicode.Progetto_Backend.service.ImmobileService;
import com.epicode.Progetto_Backend.service.LocatarioService;
import com.epicode.Progetto_Backend.service.ManutenzioneService;
import com.epicode.Progetto_Backend.service.RataService;
import com.epicode.Progetto_Backend.service.UserService;

/**
 * QueryResolver - Risolutore GraphQL per tutte le query (operazioni di lettura).
 * 
 * Implementa tutte le query definite nello schema GraphQL (schema.graphqls).
 * Ogni metodo annotato con @QueryMapping corrisponde a una query nello schema.
 * 
 * Le query supportano autenticazione e autorizzazione tramite @PreAuthorize:
 * - ADMIN: Accesso completo a tutte le query
 * - MANAGER: Accesso a query relative a immobili, contratti, locatari, rate, manutenzioni
 * - LOCATARIO: Accesso limitato (generalmente solo ai propri dati tramite query specifiche)
 * - isAuthenticated(): Qualsiasi utente autenticato
 * 
 * Le query di lista utilizzano PageRequest per ottenere tutti gli elementi
 * (senza paginazione, restituiscono l'intera lista).
 * 
 * Endpoint GraphQL: POST /graphql
 * 
 * @see src/main/resources/graphql/schema.graphqls
 */
@Controller
public class QueryResolver {

    @Autowired
    private UserService userService;

    @Autowired
    private ImmobileService immobileService;

    @Autowired
    private ContrattoService contrattoService;

    @Autowired
    private LocatarioService locatarioService;

    @Autowired
    private RataService rataService;

    @Autowired
    private ManutenzioneService manutenzioneService;

    // ==================== User Queries ====================
    
    /**
     * Query GraphQL: users - Ottiene tutti gli utenti del sistema.
     * 
     * @return Lista di tutti gli utenti
     */
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> users() {
        return userService.getAllUsers();
    }

    /**
     * Query GraphQL: user(id) - Ottiene un utente specifico per ID.
     * 
     * @param id ID dell'utente da recuperare
     * @return Utente con l'ID specificato
     */
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User user(@Argument Long id) {
        return userService.getUserById(id);
    }

    /**
     * Query GraphQL: me - Ottiene l'utente corrente autenticato.
     * 
     * Utilizza l'email dell'utente autenticato (da Authentication) per recuperare
     * i dati completi dell'utente.
     * 
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Utente corrente completo con ruoli e informazioni
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public User me(Authentication authentication) {
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }

    // ==================== Immobile Queries ====================
    
    /**
     * Query GraphQL: immobili - Ottiene tutti gli immobili del sistema.
     * 
     * @return Lista di tutti gli immobili (senza paginazione)
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Immobile> immobili() {
        return immobileService.getAllImmobili(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    /**
     * Query GraphQL: immobile(id) - Ottiene un immobile specifico per ID.
     * 
     * @param id ID dell'immobile da recuperare
     * @return Immobile completo con tutti i dettagli specifici del tipo
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Immobile immobile(@Argument Long id) {
        return immobileService.getImmobileById(id);
    }

    // ==================== Contratto Queries ====================
    
    /**
     * Query GraphQL: contratti - Ottiene tutti i contratti del sistema.
     * 
     * @return Lista di tutti i contratti (senza paginazione)
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Contratto> contratti() {
        return contrattoService.getAllContratti(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    /**
     * Query GraphQL: contratto(id) - Ottiene un contratto specifico per ID.
     * 
     * @param id ID del contratto da recuperare
     * @return Contratto completo con locatario, immobile e rate
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Contratto contratto(@Argument Long id) {
        return contrattoService.getContrattoById(id);
    }

    // ==================== Locatario Queries ====================
    
    /**
     * Query GraphQL: locatari - Ottiene tutti i locatari del sistema.
     * 
     * @return Lista di tutti i locatari (senza paginazione)
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Locatario> locatari() {
        return locatarioService.getAllLocatari(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    /**
     * Query GraphQL: locatario(id) - Ottiene un locatario specifico per ID.
     * 
     * @param id ID del locatario da recuperare
     * @return Locatario completo con dati anagrafici e riferimenti
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Locatario locatario(@Argument Long id) {
        return locatarioService.getLocatarioById(id);
    }

    // ==================== Rata Queries ====================
    
    /**
     * Query GraphQL: rate - Ottiene tutte le rate del sistema.
     * 
     * @return Lista di tutte le rate
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Rata> rate() {
        return rataService.getAllRate();
    }

    /**
     * Query GraphQL: rata(id) - Ottiene una rata specifica per ID.
     * 
     * @param id ID della rata da recuperare
     * @return Rata completa con riferimento al contratto
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Rata rata(@Argument Long id) {
        return rataService.getRataById(id);
    }

    /**
     * Query GraphQL: rateByContratto(contrattoId) - Ottiene tutte le rate di un contratto.
     * 
     * @param contrattoId ID del contratto
     * @return Lista di rate del contratto specificato
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Rata> rateByContratto(@Argument Long contrattoId) {
        return rataService.getRateByContrattoId(contrattoId);
    }

    // ==================== Manutenzione Queries ====================
    
    /**
     * Query GraphQL: manutenzioni - Ottiene tutte le manutenzioni del sistema.
     * 
     * @return Lista di tutte le manutenzioni
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Manutenzione> manutenzioni() {
        return manutenzioneService.getAllManutenzioni();
    }

    /**
     * Query GraphQL: manutenzione(id) - Ottiene una manutenzione specifica per ID.
     * 
     * @param id ID della manutenzione da recuperare
     * @return Manutenzione completa con riferimento a immobile e locatario
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Manutenzione manutenzione(@Argument Long id) {
        return manutenzioneService.getManutenzioneById(id);
    }

    // ==================== Role Queries ====================
    
    /**
     * Query GraphQL: roles - Ottiene tutti i ruoli disponibili nel sistema.
     * 
     * @return Lista di tutti i ruoli (ROLE_ADMIN, ROLE_MANAGER, ROLE_LOCATARIO)
     */
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Role> roles() {
        return userService.getAllRoles();
    }
}
