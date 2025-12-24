package com.epicode.Progetto_Backend.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.entity.Appartamento;
import com.epicode.Progetto_Backend.entity.Contratto;
import com.epicode.Progetto_Backend.entity.FrequenzaRata;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Manutenzione;
import com.epicode.Progetto_Backend.entity.Negozio;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.TipoImmobile;
import com.epicode.Progetto_Backend.entity.Ufficio;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.AppartamentoRepository;
import com.epicode.Progetto_Backend.repository.ContrattoRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.ManutenzioneRepository;
import com.epicode.Progetto_Backend.repository.NegozioRepository;
import com.epicode.Progetto_Backend.repository.RataRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UfficioRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * DataSeeder - Popola il database con dati iniziali all'avvio dell'applicazione.
 * 
 * Questa classe implementa CommandLineRunner e viene eseguita automaticamente all'avvio
 * dell'applicazione Spring Boot per inizializzare il database con dati di esempio/test.
 * 
 * Funzionalità:
 * - Crea i ruoli base del sistema (ROLE_ADMIN, ROLE_MANAGER, ROLE_LOCATARIO)
 * - Crea utenti di test con password hashate (BCrypt)
 * - Crea locatari associati agli utenti
 * - Crea immobili di esempio (appartamenti, negozi, uffici)
 * - Crea contratti di affitto associati a locatari e immobili
 * - Genera automaticamente le rate per ogni contratto in base alla frequenza
 * - Crea manutenzioni di esempio (ordinarie e straordinarie)
 * 
 * Comportamento:
 * - Viene eseguito solo se il database è vuoto (controlla count() > 0)
 * - Utilizza @Transactional per garantire consistenza dei dati
 * - Utilizza @Order(1) per essere eseguito per primo tra i CommandLineRunner
 * 
 * Dati creati:
 * - 2 Admin, 2 Manager, 4 Locatari (utenti)
 * - 4 Locatari (entità)
 * - 5 Appartamenti, 4 Negozi, 4 Uffici
 * - 4 Contratti con rate generate automaticamente
 * - 6 Manutenzioni
 * 
 * Credenziali di test (password: vedi codice):
 * - Admin: admin@cooperativa.it / admin123
 * - Manager: manager@cooperativa.it / manager123
 * - Locatario: giuseppe.verdi@email.it / locatario123
 */
@Component
@Order(1)
@Slf4j
public class DataSeeder implements CommandLineRunner {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private LocatarioRepository locatarioRepository;
    @Autowired private AppartamentoRepository appartamentoRepository;
    @Autowired private NegozioRepository negozioRepository;
    @Autowired private UfficioRepository ufficioRepository;
    @Autowired private ContrattoRepository contrattoRepository;
    @Autowired private RataRepository rataRepository;
    @Autowired private ManutenzioneRepository manutenzioneRepository;

    private final List<User> locatarioUsers = new ArrayList<>();
    private final List<Locatario> locatari = new ArrayList<>();
    private final List<Appartamento> appartamenti = new ArrayList<>();
    private final List<Negozio> negozi = new ArrayList<>();
    private final List<Ufficio> uffici = new ArrayList<>();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== DataSeeder: Inizializzazione dati completi ===");

        createRoles();
        createUsers();
        createLocatari();
        createImmobili();
        createContratti();
        createManutenzioni();

        log.info("=== DataSeeder: Completato con successo ===");
    }

    /**
     * Crea i ruoli base del sistema se non esistono già.
     * 
     * Ruoli creati:
     * - ROLE_ADMIN: Amministratore con accesso completo
     * - ROLE_MANAGER: Manager con permessi di gestione
     * - ROLE_LOCATARIO: Locatario con accesso limitato ai propri dati
     */
    private void createRoles() {
        String[] roleNames = {"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_LOCATARIO"};
        for (String roleName : roleNames) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder().name(roleName).build();
                if (role != null) {
                    roleRepository.save(role);
                    log.info("Ruolo creato: {}", roleName);
                }
            }
        }
    }

    /**
     * Crea gli utenti di test con ruoli diversi.
     * 
     * Utenti creati:
     * - 2 Admin: admin@cooperativa.it, admin2@cooperativa.it (password: admin123)
     * - 2 Manager: manager@cooperativa.it, manager2@cooperativa.it (password: manager123)
     * - 4 Locatari: giuseppe.verdi@email.it, anna.ferrari@email.it, etc. (password: locatario123)
     * 
     * Gli utenti locatario vengono salvati nella lista locatarioUsers per essere
     * utilizzati successivamente nella creazione delle entità Locatario.
     */
    private void createUsers() {
        // 2 Admin
        createUser("admin@cooperativa.it", "admin123", "Admin", "Sistema", "ROLE_ADMIN");
        createUser("admin2@cooperativa.it", "admin123", "Laura", "Bianchi", "ROLE_ADMIN");

        // 2 Manager
        createUser("manager@cooperativa.it", "manager123", "Mario", "Rossi", "ROLE_MANAGER");
        createUser("manager2@cooperativa.it", "manager123", "Giulia", "Neri", "ROLE_MANAGER");

        // 4 Locatari (utenti)
        locatarioUsers.add(createUser("giuseppe.verdi@email.it", "locatario123", "Giuseppe", "Verdi", "ROLE_LOCATARIO"));
        locatarioUsers.add(createUser("anna.ferrari@email.it", "locatario123", "Anna", "Ferrari", "ROLE_LOCATARIO"));
        locatarioUsers.add(createUser("marco.colombo@email.it", "locatario123", "Marco", "Colombo", "ROLE_LOCATARIO"));
        locatarioUsers.add(createUser("lucia.romano@email.it", "locatario123", "Lucia", "Romano", "ROLE_LOCATARIO"));
    }

    /**
     * Crea un nuovo utente se non esiste già (controllo tramite email).
     * 
     * @param email Email univoca dell'utente
     * @param password Password in plaintext (verrà hashata con BCrypt)
     * @param nome Nome dell'utente
     * @param cognome Cognome dell'utente
     * @param roleName Nome del ruolo da assegnare (deve esistere)
     * @return Utente creato o esistente
     */
    @SuppressWarnings("null")
    private User createUser(String email, String password, String nome, String cognome, String roleName) {
        if (userRepository.findByEmail(email).isEmpty()) {
            Role role = roleRepository.findByName(roleName).orElseThrow();
            User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password)) // Password hashata con BCrypt
                .nome(nome).cognome(cognome).enabled(true)
                .roles(new HashSet<>(Set.of(role))).build();
            User savedUser = userRepository.save(user);
            log.info("Utente creato: {} [{}]", email, roleName);
            return savedUser;
        }
        return userRepository.findByEmail(email).get();
    }

    /**
     * Crea le entità Locatario associate agli utenti locatario.
     * 
     * Crea 4 locatari con dati anagrafici completi (nome, cognome, codice fiscale,
     * indirizzo, telefono) associati agli utenti creati in createUsers().
     * 
     * I locatari vengono salvati nella lista locatari per essere utilizzati
     * successivamente nella creazione dei contratti.
     */
    @SuppressWarnings("null")
    private void createLocatari() {
        if (locatarioRepository.count() > 0) return;

        String[][] dati = {
            {"Giuseppe", "Verdi", "VRDGPP80A01F205X", "Via Roma 10, Milano", "+39 333 1234567"},
            {"Anna", "Ferrari", "FRRNNA85B41H501Y", "Corso Italia 25, Bergamo", "+39 349 9876543"},
            {"Marco", "Colombo", "CLMMRC78C15L388Z", "Via Dante 5, Brescia", "+39 320 5551234"},
            {"Lucia", "Romano", "RMNLCU92D55F205W", "Piazza Garibaldi 8, Como", "+39 347 6667890"}
        };

        for (int i = 0; i < dati.length && i < locatarioUsers.size(); i++) {
            Locatario loc = Locatario.builder()
                .nome(dati[i][0]).cognome(dati[i][1]).cf(dati[i][2])
                .indirizzo(dati[i][3]).telefono(dati[i][4])
                .user(locatarioUsers.get(i)).build();
            Locatario savedLoc = locatarioRepository.save(loc);
            if (savedLoc != null) {
                locatari.add(savedLoc);
            }
            log.info("Locatario creato: {} {} [{}]", dati[i][0], dati[i][1], dati[i][2]);
        }
    }

    /**
     * Crea gli immobili di esempio (appartamenti, negozi, uffici).
     * 
     * Crea:
     * - 5 Appartamenti: in diverse città (Milano, Bergamo, Brescia) con varie caratteristiche
     * - 4 Negozi: con vetrine e magazzini di dimensioni diverse
     * - 4 Uffici: con diversi posti di lavoro e sale riunioni
     * 
     * Gli immobili vengono salvati nelle liste corrispondenti per essere utilizzati
     * successivamente nella creazione dei contratti.
     */
    private void createImmobili() {
        if (appartamentoRepository.count() > 0) return;

        // 5 Appartamenti
        appartamenti.add(createAppartamento("Via Garibaldi 15", "Milano", 85.5, 3, 3));
        appartamenti.add(createAppartamento("Piazza Duomo 1", "Milano", 120.0, 5, 4));
        appartamenti.add(createAppartamento("Via Mazzini 22", "Bergamo", 70.0, 2, 2));
        appartamenti.add(createAppartamento("Corso Como 45", "Milano", 95.0, 4, 3));
        appartamenti.add(createAppartamento("Via Verdi 8", "Brescia", 65.0, 1, 2));

        // 4 Negozi
        negozi.add(createNegozio("Corso Buenos Aires 200", "Milano", 150.0, 3, 40.0));
        negozi.add(createNegozio("Via XX Settembre 50", "Brescia", 80.0, 2, 20.0));
        negozi.add(createNegozio("Via Torino 100", "Milano", 200.0, 5, 60.0));
        negozi.add(createNegozio("Corso Vittorio Emanuele 30", "Bergamo", 100.0, 2, 25.0));

        // 4 Uffici
        uffici.add(createUfficio("Via Dante 50", "Milano", 180.0, 12, 2));
        uffici.add(createUfficio("Viale Monza 100", "Milano", 250.0, 20, 3));
        uffici.add(createUfficio("Via San Marco 15", "Bergamo", 120.0, 8, 1));
        uffici.add(createUfficio("Via Trento 22", "Brescia", 160.0, 10, 2));
    }

    private Appartamento createAppartamento(String indirizzo, String citta, Double mq, int piano, int camere) {
        Appartamento a = new Appartamento();
        a.setIndirizzo(indirizzo); a.setCitta(citta); a.setSuperficie(mq);
        a.setTipo(TipoImmobile.APPARTAMENTO); a.setPiano(piano); a.setNumCamere(camere);
        log.info("Appartamento creato: {}, {} - {}mq, piano {}, {} camere", indirizzo, citta, mq, piano, camere);
        return appartamentoRepository.save(a);
    }

    private Negozio createNegozio(String indirizzo, String citta, Double mq, int vetrine, Double magazzino) {
        Negozio n = new Negozio();
        n.setIndirizzo(indirizzo); n.setCitta(citta); n.setSuperficie(mq);
        n.setTipo(TipoImmobile.NEGOZIO); n.setVetrine(vetrine); n.setMagazzinoMq(magazzino);
        log.info("Negozio creato: {}, {} - {}mq, {} vetrine, {}mq magazzino", indirizzo, citta, mq, vetrine, magazzino);
        return negozioRepository.save(n);
    }

    private Ufficio createUfficio(String indirizzo, String citta, Double mq, int posti, int sale) {
        Ufficio u = new Ufficio();
        u.setIndirizzo(indirizzo); u.setCitta(citta); u.setSuperficie(mq);
        u.setTipo(TipoImmobile.UFFICIO); u.setPostiLavoro(posti); u.setSaleRiunioni(sale);
        log.info("Ufficio creato: {}, {} - {}mq, {} posti, {} sale", indirizzo, citta, mq, posti, sale);
        return ufficioRepository.save(u);
    }

    private void createContratti() {
        if (contrattoRepository.count() > 0 || locatari.isEmpty()) return;

        // Contratto 1: Giuseppe Verdi - Appartamento Milano - TRIMESTRALE
        createContrattoConRate(locatari.get(0), appartamenti.get(0),
            LocalDate.of(2024, 1, 1), 3, 12000.0, FrequenzaRata.TRIMESTRALE);

        // Contratto 2: Anna Ferrari - Negozio Milano - MENSILE
        createContrattoConRate(locatari.get(1), negozi.get(0),
            LocalDate.of(2024, 3, 1), 5, 24000.0, FrequenzaRata.MENSILE);

        // Contratto 3: Marco Colombo - Ufficio Milano - SEMESTRALE
        createContrattoConRate(locatari.get(2), uffici.get(0),
            LocalDate.of(2023, 6, 1), 4, 18000.0, FrequenzaRata.SEMESTRALE);

        // Contratto 4: Lucia Romano - Appartamento Bergamo - ANNUALE
        createContrattoConRate(locatari.get(3), appartamenti.get(2),
            LocalDate.of(2024, 1, 1), 2, 8400.0, FrequenzaRata.ANNUALE);
    }

    /**
     * Crea un contratto di affitto e genera automaticamente tutte le rate corrispondenti.
     * 
     * Il metodo:
     * 1. Crea il contratto con le informazioni fornite
     * 2. Calcola il numero di rate in base alla frequenza e durata
     * 3. Genera tutte le rate con importo, data di scadenza e stato di pagamento
     * 
     * Logica di generazione rate:
     * - Rate per anno: MENSILE=12, BIMESTRALE=6, TRIMESTRALE=4, SEMESTRALE=2, ANNUALE=1
     * - Importo rata: canone annuo / rate per anno
     * - Data scadenza: calcolata in base alla frequenza partendo dalla data inizio
     * - Stato pagamento: 'S' se scaduta prima di oggi, 'N' se non ancora scaduta
     * - Simula morosità: ogni 5ª rata scaduta viene marcata come non pagata
     * 
     * @param loc Locatario del contratto
     * @param imm Immobile affittato
     * @param inizio Data di inizio contratto
     * @param anni Durata del contratto in anni
     * @param canone Canone annuo di affitto
     * @param freq Frequenza di pagamento delle rate
     */
    @SuppressWarnings("null")
    private void createContrattoConRate(Locatario loc, Immobile imm, LocalDate inizio,
                                         int anni, double canone, FrequenzaRata freq) {
        Contratto c = Contratto.builder()
            .locatario(loc).immobile(imm).dataInizio(inizio)
            .durataAnni(anni).canoneAnnuo(canone).frequenzaRata(freq).build();
        c = contrattoRepository.save(c);
        log.info("Contratto creato: {} {} -> {} [{}]", loc.getNome(), loc.getCognome(),
            imm.getIndirizzo(), freq);

        // Genera rate
        int ratePerAnno = switch (freq) {
            case MENSILE -> 12;
            case BIMESTRALE -> 6;
            case TRIMESTRALE -> 4;
            case SEMESTRALE -> 2;
            case ANNUALE -> 1;
        };
        int mesiTraRate = 12 / ratePerAnno;
        double importoRata = canone / ratePerAnno;
        int totaleRate = ratePerAnno * anni;
        LocalDate oggi = LocalDate.now();

        for (int i = 1; i <= totaleRate; i++) {
            LocalDate scadenza = inizio.plusMonths((long) (i - 1) * mesiTraRate + mesiTraRate);
            char pagata = scadenza.isBefore(oggi) ? 'S' : 'N';
            // Alcune rate scadute non pagate (morosità)
            if (pagata == 'S' && i % 5 == 0) pagata = 'N';

            Rata rata = Rata.builder()
                .contratto(c).numeroRata(i).dataScadenza(scadenza)
                .importo(importoRata).pagata(pagata).build();
            rataRepository.save(rata);
        }
        log.info("  -> {} rate generate (€{}/rata)", totaleRate, String.format("%.2f", importoRata));
    }

    private void createManutenzioni() {
        if (manutenzioneRepository.count() > 0 || locatari.isEmpty()) return;

        // 6 Manutenzioni diverse
        createManutenzione(appartamenti.get(0), locatari.get(0), LocalDate.of(2024, 2, 15),
            350.0, "ORDINARIA", "Riparazione rubinetto cucina e sostituzione guarnizioni");
        createManutenzione(appartamenti.get(0), locatari.get(0), LocalDate.of(2024, 6, 20),
            1800.0, "STRAORDINARIA", "Sostituzione caldaia con modello a condensazione");
        createManutenzione(negozi.get(0), locatari.get(1), LocalDate.of(2024, 4, 10),
            2500.0, "STRAORDINARIA", "Rifacimento impianto elettrico zona casse");
        createManutenzione(uffici.get(0), locatari.get(2), LocalDate.of(2024, 5, 5),
            450.0, "ORDINARIA", "Manutenzione impianto climatizzazione");
        createManutenzione(appartamenti.get(2), locatari.get(3), LocalDate.of(2024, 7, 12),
            280.0, "ORDINARIA", "Sostituzione serratura porta ingresso");
        createManutenzione(negozi.get(0), locatari.get(1), LocalDate.of(2024, 9, 1),
            3200.0, "STRAORDINARIA", "Sostituzione vetrina principale");
    }

    @SuppressWarnings("null")
    private void createManutenzione(Immobile imm, Locatario loc, LocalDate data,
                                     Double importo, String tipo, String desc) {
        Manutenzione m = Manutenzione.builder()
            .immobile(imm).locatario(loc).dataMan(data)
            .importo(importo).tipo(tipo).descrizione(desc).build();
        manutenzioneRepository.save(m);
        log.info("Manutenzione creata: {} - {} €{}", imm.getIndirizzo(), tipo, importo);
    }
}

