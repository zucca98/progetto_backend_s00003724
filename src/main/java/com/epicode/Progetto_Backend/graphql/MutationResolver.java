package com.epicode.Progetto_Backend.graphql;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.epicode.Progetto_Backend.dto.ContrattoRequestDTO;
import com.epicode.Progetto_Backend.dto.ImmobileRequestDTO;
import com.epicode.Progetto_Backend.dto.LocatarioRequestDTO;
import com.epicode.Progetto_Backend.dto.ManutenzioneRequestDTO;
import com.epicode.Progetto_Backend.dto.RataRequestDTO;
import com.epicode.Progetto_Backend.dto.UserUpdateDTO;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Manutenzione;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.graphql.input.ContrattoInput;
import com.epicode.Progetto_Backend.graphql.input.ImmobileInput;
import com.epicode.Progetto_Backend.graphql.input.LocatarioInput;
import com.epicode.Progetto_Backend.graphql.input.ManutenzioneInput;
import com.epicode.Progetto_Backend.graphql.input.RataInput;
import com.epicode.Progetto_Backend.graphql.input.UserUpdateInput;
import com.epicode.Progetto_Backend.service.ContrattoService;
import com.epicode.Progetto_Backend.service.ImmobileService;
import com.epicode.Progetto_Backend.service.LocatarioService;
import com.epicode.Progetto_Backend.service.ManutenzioneService;
import com.epicode.Progetto_Backend.service.RataService;
import com.epicode.Progetto_Backend.service.UserService;

/**
 * MutationResolver - Risolutore GraphQL per tutte le mutation (operazioni di scrittura).
 * 
 * Implementa tutte le mutation definite nello schema GraphQL (schema.graphqls).
 * Ogni metodo annotato con @MutationMapping corrisponde a una mutation nello schema.
 * 
 * Le mutation supportano autenticazione e autorizzazione tramite @PreAuthorize:
 * - ADMIN: Accesso completo a tutte le mutation (incluso DELETE)
 * - MANAGER: Può creare e aggiornare (no DELETE)
 * - LOCATARIO: Può aggiornare solo il proprio profilo (updateMe)
 * 
 * Le mutation convertono gli Input GraphQL (ImmobileInput, ContrattoInput, etc.)
 * nei corrispondenti DTO utilizzati dai servizi REST.
 * 
 * Le date vengono parse da String a LocalDate (formato ISO: YYYY-MM-DD).
 * 
 * Endpoint GraphQL: POST /graphql
 * 
 * @see src/main/resources/graphql/schema.graphqls
 * @see com.epicode.Progetto_Backend.graphql.input
 */
@Controller
public class MutationResolver {

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

    // ==================== User Mutations ====================
    
    /**
     * Mutation GraphQL: updateUser(id, input) - Aggiorna un utente specifico.
     * 
     * @param id ID dell'utente da aggiornare
     * @param input Input con i campi da aggiornare (nome, cognome, profileImage)
     * @return Utente aggiornato
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@Argument Long id, @Argument UserUpdateInput input) {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNome(input.getNome());
        dto.setCognome(input.getCognome());
        dto.setProfileImage(input.getProfileImage());
        return userService.updateUser(id, dto);
    }

    /**
     * Mutation GraphQL: updateMe(input) - Aggiorna il profilo dell'utente corrente.
     * 
     * @param input Input con i campi da aggiornare
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Utente aggiornato
     */
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public User updateMe(@Argument UserUpdateInput input, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNome(input.getNome());
        dto.setCognome(input.getCognome());
        dto.setProfileImage(input.getProfileImage());
        return userService.updateUser(currentUser.getId(), dto);
    }

    /**
     * Mutation GraphQL: updateUserRoles(id, roles) - Aggiorna i ruoli di un utente.
     * 
     * @param id ID dell'utente
     * @param roles Set di nomi ruoli da assegnare (es: ["ROLE_ADMIN", "ROLE_MANAGER"])
     * @return Utente con i ruoli aggiornati
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserRoles(@Argument Long id, @Argument Set<String> roles) {
        return userService.updateUserRoles(id, roles);
    }

    /**
     * Mutation GraphQL: deleteUser(id) - Elimina un utente dal sistema.
     * 
     * @param id ID dell'utente da eliminare
     * @return true se l'eliminazione è riuscita
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteUser(@Argument Long id) {
        userService.deleteUser(id);
        return true;
    }

    // ==================== Immobile Mutations ====================
    
    /**
     * Mutation GraphQL: createImmobile(input) - Crea un nuovo immobile.
     * 
     * Il tipo di immobile determina quali campi sono utilizzati:
     * - APPARTAMENTO: piano, numCamere
     * - NEGOZIO: vetrine, magazzinoMq
     * - UFFICIO: postiLavoro, saleRiunioni
     * 
     * @param input Input con i dati dell'immobile da creare
     * @return Immobile creato con ID assegnato
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Immobile createImmobile(@Argument ImmobileInput input) {
        ImmobileRequestDTO dto = ImmobileRequestDTO.builder()
                .indirizzo(input.getIndirizzo())
                .citta(input.getCitta())
                .superficie(input.getSuperficie())
                .tipo(input.getTipo())
                .piano(input.getPiano())
                .numCamere(input.getNumCamere())
                .vetrine(input.getVetrine())
                .magazzinoMq(input.getMagazzinoMq())
                .postiLavoro(input.getPostiLavoro())
                .saleRiunioni(input.getSaleRiunioni())
                .build();
        return immobileService.createImmobile(dto);
    }

    /**
     * Mutation GraphQL: updateImmobile(id, input) - Aggiorna un immobile esistente.
     * 
     * @param id ID dell'immobile da aggiornare
     * @param input Input con i dati aggiornati
     * @return Immobile aggiornato
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Immobile updateImmobile(@Argument Long id, @Argument ImmobileInput input) {
        ImmobileRequestDTO dto = ImmobileRequestDTO.builder()
                .indirizzo(input.getIndirizzo())
                .citta(input.getCitta())
                .superficie(input.getSuperficie())
                .tipo(input.getTipo())
                .piano(input.getPiano())
                .numCamere(input.getNumCamere())
                .vetrine(input.getVetrine())
                .magazzinoMq(input.getMagazzinoMq())
                .postiLavoro(input.getPostiLavoro())
                .saleRiunioni(input.getSaleRiunioni())
                .build();
        return immobileService.updateImmobile(id, dto);
    }

    /**
     * Mutation GraphQL: deleteImmobile(id) - Elimina un immobile dal sistema.
     * 
     * @param id ID dell'immobile da eliminare
     * @return true se l'eliminazione è riuscita
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Boolean deleteImmobile(@Argument Long id) {
        immobileService.deleteImmobile(id);
        return true;
    }

    // ==================== Contratto Mutations ====================
    
    /**
     * Mutation GraphQL: createContratto(input) - Crea un nuovo contratto di affitto.
     * 
     * Alla creazione, il sistema genera automaticamente tutte le rate
     * in base alla frequenza e durata del contratto.
     * La dataInizio deve essere in formato ISO (YYYY-MM-DD).
     * 
     * @param input Input con dati del contratto (locatarioId, immobileId, dataInizio, durataAnni, canoneAnnuo, frequenzaRata)
     * @return Contratto creato con ID e rate generate
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Contratto createContratto(@Argument ContrattoInput input) {
        ContrattoRequestDTO dto = ContrattoRequestDTO.builder()
                .locatarioId(input.getLocatarioId())
                .immobileId(input.getImmobileId())
                .dataInizio(LocalDate.parse(input.getDataInizio()))
                .durataAnni(input.getDurataAnni())
                .canoneAnnuo(input.getCanoneAnnuo())
                .frequenzaRata(input.getFrequenzaRata())
                .build();
        return contrattoService.createContratto(dto);
    }

    /**
     * Mutation GraphQL: updateContratto(id, input) - Aggiorna un contratto esistente.
     * 
     * La dataInizio deve essere in formato ISO (YYYY-MM-DD).
     * 
     * @param id ID del contratto da aggiornare
     * @param input Input con i dati aggiornati
     * @return Contratto aggiornato
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Contratto updateContratto(@Argument Long id, @Argument ContrattoInput input) {
        ContrattoRequestDTO dto = ContrattoRequestDTO.builder()
                .locatarioId(input.getLocatarioId())
                .immobileId(input.getImmobileId())
                .dataInizio(LocalDate.parse(input.getDataInizio()))
                .durataAnni(input.getDurataAnni())
                .canoneAnnuo(input.getCanoneAnnuo())
                .frequenzaRata(input.getFrequenzaRata())
                .build();
        return contrattoService.updateContratto(id, dto);
    }

    /**
     * Mutation GraphQL: deleteContratto(id) - Elimina un contratto dal sistema.
     * 
     * @param id ID del contratto da eliminare
     * @return true se l'eliminazione è riuscita
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Boolean deleteContratto(@Argument Long id) {
        contrattoService.deleteContratto(id);
        return true;
    }

    // ==================== Locatario Mutations ====================
    
    /**
     * Mutation GraphQL: createLocatario(input) - Crea un nuovo locatario.
     * 
     * Il locatario viene associato a un utente esistente tramite userId.
     * Il codice fiscale deve essere univoco.
     * 
     * @param input Input con dati del locatario (nome, cognome, cf, indirizzo, telefono, userId)
     * @return Locatario creato con ID assegnato
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Locatario createLocatario(@Argument LocatarioInput input) {
        LocatarioRequestDTO dto = LocatarioRequestDTO.builder()
                .nome(input.getNome())
                .cognome(input.getCognome())
                .cf(input.getCf())
                .indirizzo(input.getIndirizzo())
                .telefono(input.getTelefono())
                .userId(input.getUserId())
                .build();
        return locatarioService.createLocatario(dto);
    }

    /**
     * Mutation GraphQL: updateLocatario(id, input) - Aggiorna un locatario esistente.
     * 
     * @param id ID del locatario da aggiornare
     * @param input Input con i dati aggiornati
     * @return Locatario aggiornato
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Locatario updateLocatario(@Argument Long id, @Argument LocatarioInput input) {
        LocatarioRequestDTO dto = LocatarioRequestDTO.builder()
                .nome(input.getNome())
                .cognome(input.getCognome())
                .cf(input.getCf())
                .indirizzo(input.getIndirizzo())
                .telefono(input.getTelefono())
                .userId(input.getUserId())
                .build();
        return locatarioService.updateLocatario(id, dto);
    }

    /**
     * Mutation GraphQL: deleteLocatario(id) - Elimina un locatario dal sistema.
     * 
     * @param id ID del locatario da eliminare
     * @return true se l'eliminazione è riuscita
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Boolean deleteLocatario(@Argument Long id) {
        locatarioService.deleteLocatario(id);
        return true;
    }

    // ==================== Rata Mutations ====================
    
    /**
     * Mutation GraphQL: createRata(input) - Crea una nuova rata.
     * 
     * Nota: Le rate vengono generalmente generate automaticamente alla creazione
     * di un contratto. Questa mutation è utile per rate manuali o aggiuntive.
     * 
     * La dataScadenza deve essere in formato ISO (YYYY-MM-DD).
     * Il campo pagata viene convertito da Boolean a Character ('S' o 'N').
     * 
     * @param input Input con dati della rata (contrattoId, numeroRata, dataScadenza, importo, pagata)
     * @return Rata creata con ID assegnato
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Rata createRata(@Argument RataInput input) {
        RataRequestDTO dto = RataRequestDTO.builder()
                .contrattoId(input.getContrattoId())
                .numeroRata(input.getNumeroRata())
                .dataScadenza(LocalDate.parse(input.getDataScadenza()))
                .importo(input.getImporto())
                .pagata(input.getPagata() != null && input.getPagata() ? 'S' : 'N')
                .build();
        return rataService.createRata(dto);
    }

    /**
     * Mutation GraphQL: updateRata(id, input) - Aggiorna una rata esistente.
     * 
     * La dataScadenza deve essere in formato ISO (YYYY-MM-DD).
     * Il campo pagata viene convertito da Boolean a Character ('S' o 'N').
     * 
     * @param id ID della rata da aggiornare
     * @param input Input con i dati aggiornati
     * @return Rata aggiornata
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Rata updateRata(@Argument Long id, @Argument RataInput input) {
        RataRequestDTO dto = RataRequestDTO.builder()
                .contrattoId(input.getContrattoId())
                .numeroRata(input.getNumeroRata())
                .dataScadenza(LocalDate.parse(input.getDataScadenza()))
                .importo(input.getImporto())
                .pagata(input.getPagata() != null && input.getPagata() ? 'S' : 'N')
                .build();
        return rataService.updateRata(id, dto);
    }

    /**
     * Mutation GraphQL: markRataAsPagata(id) - Marca una rata come pagata.
     * 
     * Utility mutation per aggiornare rapidamente lo stato di pagamento.
     * Equivalente a updateRata con pagata = true.
     * 
     * @param id ID della rata da marcare come pagata
     * @return Rata aggiornata con pagata = 'S'
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Rata markRataAsPagata(@Argument Long id) {
        return rataService.updateRataPagata(id, 'S');
    }

    /**
     * Mutation GraphQL: deleteRata(id) - Elimina una rata dal sistema.
     * 
     * @param id ID della rata da eliminare
     * @return true se l'eliminazione è riuscita
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Boolean deleteRata(@Argument Long id) {
        rataService.deleteRata(id);
        return true;
    }

    // ==================== Manutenzione Mutations ====================
    
    /**
     * Mutation GraphQL: createManutenzione(input) - Crea una nuova manutenzione.
     * 
     * La dataMan deve essere in formato ISO (YYYY-MM-DD).
     * Il tipo può essere "ORDINARIA" o "STRAORDINARIA" (default nel DTO: "STRAORDINARIA").
     * 
     * @param input Input con dati della manutenzione (immobileId, locatarioId, dataMan, importo, tipo, descrizione)
     * @return Manutenzione creata con ID assegnato
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Manutenzione createManutenzione(@Argument ManutenzioneInput input) {
        ManutenzioneRequestDTO dto = ManutenzioneRequestDTO.builder()
                .immobileId(input.getImmobileId())
                .locatarioId(input.getLocatarioId())
                .dataMan(LocalDate.parse(input.getDataMan()))
                .importo(input.getImporto())
                .tipo(input.getTipo())
                .descrizione(input.getDescrizione())
                .build();
        return manutenzioneService.createManutenzione(dto);
    }

    /**
     * Mutation GraphQL: updateManutenzione(id, input) - Aggiorna una manutenzione esistente.
     * 
     * La dataMan deve essere in formato ISO (YYYY-MM-DD).
     * 
     * @param id ID della manutenzione da aggiornare
     * @param input Input con i dati aggiornati
     * @return Manutenzione aggiornata
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Manutenzione updateManutenzione(@Argument Long id, @Argument ManutenzioneInput input) {
        ManutenzioneRequestDTO dto = ManutenzioneRequestDTO.builder()
                .immobileId(input.getImmobileId())
                .locatarioId(input.getLocatarioId())
                .dataMan(LocalDate.parse(input.getDataMan()))
                .importo(input.getImporto())
                .tipo(input.getTipo())
                .descrizione(input.getDescrizione())
                .build();
        return manutenzioneService.updateManutenzione(id, dto);
    }

    /**
     * Mutation GraphQL: deleteManutenzione(id) - Elimina una manutenzione dal sistema.
     * 
     * @param id ID della manutenzione da eliminare
     * @return true se l'eliminazione è riuscita
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Boolean deleteManutenzione(@Argument Long id) {
        manutenzioneService.deleteManutenzione(id);
        return true;
    }
}
