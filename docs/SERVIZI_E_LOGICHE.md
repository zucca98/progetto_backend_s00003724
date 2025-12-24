# Servizi e Logiche di Business

Questo documento descrive i **servizi** del progetto e le **logiche di business** implementate.

## 📋 Panoramica

I servizi implementano la **business logic** del sistema, separando la logica di dominio dai controller e dai repository. Ogni servizio gestisce un'entità o un'area funzionale specifica.

## 🏗️ Struttura Service Layer

```
Service Layer
├── AuthService              # Autenticazione e registrazione
├── UserService              # Gestione utenti
├── ImmobileService          # Gestione immobili
├── ContrattoService         # Gestione contratti
├── LocatarioService         # Gestione locatari
├── RataService              # Gestione rate
├── ManutenzioneService      # Gestione manutenzioni
├── CloudinaryService        # Upload immagini
├── MailgunService           # Invio email
└── CustomUserDetailsService # Caricamento UserDetails per Spring Security
```

## 🔐 AuthService

**Classe**: `AuthService.java`

**Responsabilità**:
- Registrazione nuovi utenti
- Autenticazione utenti esistenti
- Generazione token JWT
- Invio email di benvenuto

### Metodi Principali

#### `register(RegisterRequestDTO request)`
Registra un nuovo utente.

**Logica**:
1. Verifica che l'email non esista già
2. Hasha la password con BCrypt
3. Crea nuovo utente con `enabled = true`
4. Salva utente nel database
5. Genera token JWT
6. Invia email di benvenuto (Mailgun)
7. Restituisce `AuthResponseDTO` con token

**Note**:
- L'utente viene creato **senza ruoli** (devono essere assegnati da ADMIN)
- L'email di benvenuto viene inviata in modo asincrono (non blocca la risposta)

#### `login(LoginRequestDTO request)`
Autentica un utente esistente.

**Logica**:
1. Carica utente per email
2. Verifica che l'utente sia abilitato (`enabled = true`)
3. Valida password con BCrypt
4. Genera token JWT
5. Restituisce `AuthResponseDTO` con token

**Errori**:
- `EntityNotFoundException`: Email non trovata
- `BadCredentialsException`: Password non valida
- `BusinessException`: Utente disabilitato

---

## 👤 UserService

**Classe**: `UserService.java`

**Responsabilità**:
- CRUD utenti
- Gestione ruoli
- Aggiornamento profilo

### Metodi Principali

#### `getAllUsers()`
Ottiene tutti gli utenti.

**Logica**:
- Query semplice: `userRepository.findAll()`
- Restituisce lista completa (senza paginazione per semplicità)

#### `getUserById(Long id)`
Ottiene un utente per ID.

**Logica**:
- Query: `userRepository.findById(id)`
- Lancia `EntityNotFoundException` se non trovato

#### `getUserByEmail(String email)`
Ottiene un utente per email.

**Logica**:
- Query: `userRepository.findByEmail(email)`
- Utilizzato per autenticazione

#### `updateUser(Long id, UserUpdateDTO dto)`
Aggiorna un utente.

**Logica**:
1. Carica utente esistente
2. Aggiorna solo i campi non null nel DTO
3. Salva modifiche
4. Restituisce utente aggiornato

**Note**:
- `password` non può essere aggiornato tramite questo metodo (endpoint separato)
- `email` non può essere modificata (univoca)

#### `updateUserRoles(Long id, List<String> roleNames)`
Aggiorna i ruoli di un utente.

**Logica**:
1. Carica utente esistente
2. Carica ruoli per nome
3. Sostituisce tutti i ruoli esistenti con i nuovi
4. Salva modifiche

**Autorizzazione**: Solo ADMIN può chiamare questo metodo

#### `deleteUser(Long id)`
Elimina un utente.

**Logica**:
- `userRepository.deleteById(id)`
- Cascade: Se l'utente ha un `Locatario` associato, viene eliminato anche quello

---

## 🏠 ImmobileService

**Classe**: `ImmobileService.java`

**Responsabilità**:
- CRUD immobili
- Gestione ereditarietà (Appartamento, Negozio, Ufficio)
- Statistiche e aggregazioni

### Metodi Principali

#### `getAllImmobili(Pageable pageable)`
Ottiene tutti gli immobili paginati.

**Logica**:
- Query: `immobileRepository.findAll(pageable)`
- Restituisce `Page<Immobile>`

#### `getImmobileById(Long id)`
Ottiene un immobile per ID.

**Logica**:
- Query: `immobileRepository.findById(id)`
- Lancia `EntityNotFoundException` se non trovato

#### `createImmobile(ImmobileRequestDTO request)`
Crea un nuovo immobile.

**Logica**:
1. Determina il tipo di immobile (`tipo`)
2. Crea istanza della sottoclasse appropriata:
   - `APPARTAMENTO` → `Appartamento`
   - `NEGOZIO` → `Negozio`
   - `UFFICIO` → `Ufficio`
3. Imposta campi comuni (indirizzo, città, superficie, tipo)
4. Imposta campi specifici della sottoclasse
5. Salva nel database
6. Restituisce immobile creato

**Pattern**: Utilizza **switch expression** (Java 14+) per creare istanze corrette.

**Esempio**:
```java
Immobile immobile = switch (request.getTipo()) {
    case APPARTAMENTO -> {
        Appartamento app = new Appartamento();
        app.setPiano(request.getPiano());
        app.setNumCamere(request.getNumCamere());
        yield app;
    }
    case NEGOZIO -> { ... }
    case UFFICIO -> { ... }
};
```

#### `updateImmobile(Long id, ImmobileRequestDTO request)`
Aggiorna un immobile.

**Logica**:
1. Carica immobile esistente
2. Aggiorna campi comuni se presenti nel DTO
3. Se il tipo è cambiato, gestisce conversione (complesso)
4. Aggiorna campi specifici della sottoclasse
5. Salva modifiche

**Note**:
- La conversione tra tipi (es: Appartamento → Negozio) non è supportata
- Solo i campi non null nel DTO vengono aggiornati

#### `deleteImmobile(Long id)`
Elimina un immobile.

**Logica**:
- Verifica che non ci siano contratti attivi
- Elimina immobile (cascade elimina anche contratti e manutenzioni)

**Validazione Business**:
- Se ci sono contratti attivi, lancia `BusinessException`

#### `getImmobiliAffittatiPerCitta()`
Statistica: immobili affittati per città.

**Logica**:
- Query JPQL con GROUP BY
- Restituisce `Map<String, Long>` (città → conteggio)

#### `getContImmobiliPerTipo()`
Statistica: conteggio immobili per tipo.

**Logica**:
- Query JPQL con GROUP BY
- Restituisce `Map<TipoImmobile, Long>`

---

## 📄 ContrattoService

**Classe**: `ContrattoService.java`

**Responsabilità**:
- CRUD contratti
- Generazione automatica rate
- Validazione business rules

### Metodi Principali

#### `createContratto(ContrattoRequestDTO request)`
Crea un nuovo contratto.

**Logica**:
1. Verifica che locatario e immobile esistano
2. Verifica che l'immobile non sia già affittato (contratto attivo)
3. Crea nuovo contratto
4. **Genera automaticamente le rate** in base a:
   - `durataAnni`
   - `frequenzaRata`
   - `canoneAnnuo`
5. Salva contratto e rate
6. Invia email di notifica al locatario
7. Restituisce contratto creato

**Generazione Rate**:
```java
int numeroRateAnnue = switch (frequenzaRata) {
    case MENSILE -> 12;
    case BIMESTRALE -> 6;
    case TRIMESTRALE -> 4;
    case SEMESTRALE -> 2;
    case ANNUALE -> 1;
};

int totaleRate = durataAnni * numeroRateAnnue;
double importoRata = canoneAnnuo / numeroRateAnnue;

for (int i = 1; i <= totaleRate; i++) {
    LocalDate dataScadenza = dataInizio
        .plusMonths(i * mesiPerRata)
        .minusDays(1);
    
    Rata rata = Rata.builder()
        .contratto(contratto)
        .numeroRata(i)
        .dataScadenza(dataScadenza)
        .importo(importoRata)
        .pagata(false)
        .build();
    
    rate.add(rata);
}
```

**Validazione Business**:
- Immobile non può essere affittato se ha già un contratto attivo
- Locatario deve esistere
- Immobile deve esistere

#### `updateContratto(Long id, ContrattoRequestDTO request)`
Aggiorna un contratto.

**Logica**:
1. Carica contratto esistente
2. Aggiorna campi se presenti nel DTO
3. Se `frequenzaRata` o `durataAnni` cambiano, rigenera le rate
4. Salva modifiche

**Note**:
- La rigenerazione delle rate elimina le rate esistenti e ne crea di nuove

#### `deleteContratto(Long id)`
Elimina un contratto.

**Logica**:
- Elimina contratto (cascade elimina anche tutte le rate)

---

## 👥 LocatarioService

**Classe**: `LocatarioService.java`

**Responsabilità**:
- CRUD locatari
- Validazione codice fiscale univoco
- Associazione con User

### Metodi Principali

#### `createLocatario(LocatarioRequestDTO request)`
Crea un nuovo locatario.

**Logica**:
1. Verifica che il codice fiscale non esista già
2. Verifica che l'utente esista e non abbia già un locatario
3. Crea nuovo locatario
4. Associa locatario all'utente
5. Salva nel database
6. Restituisce locatario creato

**Validazione Business**:
- `cf` deve essere univoco
- Un utente può avere al massimo un locatario (One-to-One)

#### `getLocatarioByUserId(Long userId)`
Ottiene il locatario associato a un utente.

**Logica**:
- Query: `locatarioRepository.findByUserId(userId)`
- Utilizzato per endpoint `/api/locatari/mio` (LOCATARIO)

---

## 💰 RataService

**Classe**: `RataService.java`

**Responsabilità**:
- CRUD rate
- Marcatura rate come pagate
- Query rate per contratto

### Metodi Principali

#### `getRateByContrattoId(Long contrattoId)`
Ottiene tutte le rate di un contratto.

**Logica**:
- Query: `rataRepository.findByContrattoId(contrattoId)`
- Ordinamento per `numeroRata`

#### `markRataAsPagata(Long id)`
Segna una rata come pagata.

**Logica**:
1. Carica rata esistente
2. Imposta `pagata = true`
3. Salva modifiche
4. Restituisce rata aggiornata

**Autorizzazione**: Solo LOCATARIO può chiamare questo metodo

---

## 🔧 ManutenzioneService

**Classe**: `ManutenzioneService.java`

**Responsabilità**:
- CRUD manutenzioni
- Validazione associazione immobile-locatario

### Metodi Principali

#### `createManutenzione(ManutenzioneRequestDTO request)`
Crea una nuova manutenzione.

**Logica**:
1. Verifica che immobile e locatario esistano
2. Verifica che il locatario abbia un contratto attivo per quell'immobile
3. Crea nuova manutenzione
4. Salva nel database
5. Restituisce manutenzione creata

**Validazione Business**:
- Il locatario deve avere un contratto attivo per l'immobile specificato

---

## 📤 CloudinaryService

**Classe**: `CloudinaryService.java`

**Responsabilità**:
- Upload immagini su Cloudinary
- Gestione URL immagini profilo

### Metodi Principali

#### `uploadImage(MultipartFile file)`
Carica un'immagine su Cloudinary.

**Logica**:
1. Riceve file multipart
2. Carica su Cloudinary nella cartella `profile_images`
3. Restituisce URL pubblico dell'immagine

**Configurazione**:
- Cloud name, API key, API secret da `CloudinaryProperties`
- Folder: `profile_images`
- Resource type: `auto` (rileva automaticamente tipo file)

**Note**:
- File max size: 10MB (configurato in `application.properties`)
- Formati supportati: JPG, PNG, GIF, WebP

---

## 📧 MailgunService

**Classe**: `MailgunService.java`

**Responsabilità**:
- Invio email tramite Mailgun API
- Template email predefiniti

### Metodi Principali

#### `sendEmail(String to, String subject, String text)`
Invia un'email generica.

**Logica**:
1. Costruisce richiesta HTTP POST a Mailgun API
2. Invia email con from, to, subject, text
3. Verifica risposta (status 200)

**Configurazione**:
- API key e domain da `MailgunProperties`
- From email configurabile

#### `sendWelcomeEmail(String userEmail, String userName)`
Invia email di benvenuto a un nuovo utente.

**Template**:
```
Caro {userName},

Benvenuto nella nostra cooperativa immobiliare!

Il tuo account è stato creato con successo.

Cordiali saluti,
Il Team della Cooperativa
```

#### `sendContractNotification(String userEmail, String locatarioName, String immobileIndirizzo)`
Invia notifica di nuovo contratto.

**Template**:
```
Gentile {locatarioName},

Ti informiamo che è stato creato un nuovo contratto di affitto per l'immobile:
{immobileIndirizzo}

Cordiali saluti,
Il Team della Cooperativa
```

---

## 🔄 Transazioni

Tutti i metodi che modificano dati sono annotati con `@Transactional`:

```java
@Transactional
public Immobile createImmobile(ImmobileRequestDTO request) {
    // Operazioni database
}
```

**Caratteristiche**:
- **Propagation**: `REQUIRED` (default)
- **Isolation**: `READ_COMMITTED` (default)
- **Rollback**: Automatico in caso di eccezione

**Esempi**:
- `createContratto`: Transazione include creazione contratto + generazione rate
- `updateContratto`: Transazione include aggiornamento + rigenerazione rate

## 📝 Best Practices Implementate

✅ **Separation of Concerns**: Business logic isolata nei service  
✅ **Single Responsibility**: Ogni service gestisce un'entità specifica  
✅ **Dependency Injection**: `@Autowired` o `@RequiredArgsConstructor`  
✅ **Transaction Management**: `@Transactional` su metodi che modificano dati  
✅ **Error Handling**: Lancia eccezioni custom (`EntityNotFoundException`, `BusinessException`)  
✅ **Validation**: Validazione business rules prima di salvare  
✅ **Logging**: Logger strutturato per debug e monitoraggio

---

Per dettagli su configurazione, consulta [CONFIGURAZIONE.md](./CONFIGURAZIONE.md).

