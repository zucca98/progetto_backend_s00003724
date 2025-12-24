# Entità e Relazioni

Questo documento descrive il **modello dati** del progetto, le entità JPA e le loro relazioni.

## 📊 Modello Dati Complessivo

Il sistema gestisce una **cooperativa immobiliare** con le seguenti entità principali:

```
User ──┐
       │ (One-to-One)
       └──> Locatario ──┐
                        │
                        ├──> Contratto ──┐
                        │                │
                        │                └──> Rata
                        │
                        └──> Manutenzione
                                 │
                                 │
Immobile ──┬──> Appartamento     │
           ├──> Negozio          │
           └──> Ufficio          │
                                 │
                                 └──> (Many-to-One)
```

## 🗂️ Entità Principali

### 1. **User** (`users`)

**Tabella**: `users`

**Descrizione**: Rappresenta un utente del sistema. Implementa `UserDetails` per integrazione con Spring Security.

**Campi**:
- `id` (Long, PK, Auto-increment)
- `email` (String, UNIQUE, NOT NULL)
- `password` (String, NOT NULL, hashata con BCrypt)
- `profileImage` (String, nullable) - URL immagine profilo (Cloudinary)
- `nome` (String, NOT NULL)
- `cognome` (String, NOT NULL)
- `registrationDate` (LocalDate, default: CURRENT_DATE)
- `enabled` (Boolean, default: true)

**Relazioni**:
- **Many-to-Many** con `Role` (tabella `user_roles`)
- **One-to-One** con `Locatario` (opzionale, solo se l'utente è un locatario)

**Note**:
- La password è annotata con `@JsonIgnore` per non essere serializzata
- `getAuthorities()` restituisce i ruoli come `GrantedAuthority`
- `getUsername()` restituisce l'email

### 2. **Role** (`roles`)

**Tabella**: `roles`

**Descrizione**: Rappresenta un ruolo utente nel sistema.

**Campi**:
- `id` (Long, PK, Auto-increment)
- `name` (String, UNIQUE, NOT NULL) - Es: "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_LOCATARIO"

**Relazioni**:
- **Many-to-Many** con `User` (tabella `user_roles`)

**Ruoli Predefiniti**:
- `ROLE_ADMIN`: Accesso completo
- `ROLE_MANAGER`: Gestione immobili, contratti, locatari
- `ROLE_LOCATARIO`: Accesso limitato ai propri dati

### 3. **Immobile** (`immobile`)

**Tabella**: `immobile` (classe base)

**Descrizione**: Classe base astratta per tutti i tipi di immobili. Utilizza **ereditarietà JOINED**.

**Campi**:
- `id` (Long, PK, Auto-increment)
- `indirizzo` (String, NOT NULL)
- `citta` (String, NOT NULL)
- `superficie` (Double, NOT NULL) - in m²
- `tipo` (TipoImmobile, NOT NULL) - Enum: APPARTAMENTO, NEGOZIO, UFFICIO

**Relazioni**:
- **One-to-Many** con `Contratto`
- **One-to-Many** con `Manutenzione`

**Inheritance Strategy**: `InheritanceType.JOINED`
- Tabella base: `immobile`
- Tabelle derivate: `appartamento`, `negozio`, `ufficio`
- Ogni sottoclasse ha una foreign key verso `immobile.id`

### 4. **Appartamento** (`appartamento`)

**Tabella**: `appartamento` (sottoclasse di `Immobile`)

**Descrizione**: Rappresenta un appartamento.

**Campi Ereditati**: Tutti da `Immobile`

**Campi Specifici**:
- `immobile_id` (Long, PK, FK → `immobile.id`)
- `piano` (Integer, NOT NULL)
- `numCamere` (Integer, NOT NULL)

### 5. **Negozio** (`negozio`)

**Tabella**: `negozio` (sottoclasse di `Immobile`)

**Descrizione**: Rappresenta un negozio.

**Campi Ereditati**: Tutti da `Immobile`

**Campi Specifici**:
- `immobile_id` (Long, PK, FK → `immobile.id`)
- `vetrine` (Integer, NOT NULL)
- `magazzinoMq` (Double, NOT NULL) - superficie magazzino in m²

### 6. **Ufficio** (`ufficio`)

**Tabella**: `ufficio` (sottoclasse di `Immobile`)

**Descrizione**: Rappresenta un ufficio.

**Campi Ereditati**: Tutti da `Immobile`

**Campi Specifici**:
- `immobile_id` (Long, PK, FK → `immobile.id`)
- `postiLavoro` (Integer, NOT NULL)
- `saleRiunioni` (Integer, NOT NULL)

### 7. **Locatario** (`locatario`)

**Tabella**: `locatario`

**Descrizione**: Rappresenta un locatario (affittuario).

**Campi**:
- `id` (Long, PK, Auto-increment)
- `nome` (String, NOT NULL)
- `cognome` (String, NOT NULL)
- `cf` (String, UNIQUE, NOT NULL) - Codice Fiscale
- `indirizzo` (String, NOT NULL)
- `telefono` (String, NOT NULL)
- `user_id` (Long, UNIQUE, FK → `users.id`, NOT NULL)

**Relazioni**:
- **One-to-One** con `User` (ogni locatario ha un account utente)
- **One-to-Many** con `Contratto`
- **One-to-Many** con `Manutenzione`

**Note**:
- Un `User` può essere associato a un `Locatario` (opzionale)
- Il `cf` è univoco per evitare duplicati

### 8. **Contratto** (`contratto`)

**Tabella**: `contratto`

**Descrizione**: Rappresenta un contratto di affitto.

**Campi**:
- `id` (Long, PK, Auto-increment)
- `locatario_id` (Long, FK → `locatario.id`, NOT NULL)
- `immobile_id` (Long, FK → `immobile.id`, NOT NULL)
- `dataInizio` (LocalDate, NOT NULL)
- `durataAnni` (Integer, NOT NULL)
- `canoneAnnuo` (Double, NOT NULL) - canone annuo in euro
- `frequenzaRata` (FrequenzaRata, NOT NULL, default: TRIMESTRALE)

**Relazioni**:
- **Many-to-One** con `Locatario`
- **Many-to-One** con `Immobile`
- **One-to-Many** con `Rata`

**Note**:
- La `frequenzaRata` determina quante rate vengono generate all'anno
- Le rate vengono generate automaticamente alla creazione del contratto

### 9. **Rata** (`rata`)

**Tabella**: `rata`

**Descrizione**: Rappresenta una rata di affitto.

**Campi**:
- `id` (Long, PK, Auto-increment)
- `contratto_id` (Long, FK → `contratto.id`, NOT NULL)
- `numeroRata` (Integer, NOT NULL) - numero progressivo della rata
- `dataScadenza` (LocalDate, NOT NULL)
- `importo` (Double, NOT NULL) - importo della rata in euro
- `pagata` (Boolean, NOT NULL, default: false)

**Relazioni**:
- **Many-to-One** con `Contratto`

**Note**:
- Le rate vengono generate automaticamente in base a `frequenzaRata` del contratto
- `importo = canoneAnnuo / numeroRateAnnue`

### 10. **Manutenzione** (`manutenzione`)

**Tabella**: `manutenzione`

**Descrizione**: Rappresenta una manutenzione su un immobile.

**Campi**:
- `id` (Long, PK, Auto-increment)
- `immobile_id` (Long, FK → `immobile.id`, NOT NULL)
- `locatario_id` (Long, FK → `locatario.id`, NOT NULL)
- `dataMan` (LocalDate, NOT NULL) - data manutenzione
- `importo` (Double, NOT NULL) - costo manutenzione in euro
- `tipo` (String, NOT NULL) - tipo di manutenzione (es: "ORDINARIA", "STRAORDINARIA")
- `descrizione` (String, nullable)

**Relazioni**:
- **Many-to-One** con `Immobile`
- **Many-to-One** con `Locatario`

## 🔗 Relazioni Dettagliate

### One-to-One

#### User ↔ Locatario
- **User → Locatario**: `@OneToOne(mappedBy = "user")`
- **Locatario → User**: `@OneToOne @JoinColumn(name = "user_id")`
- **Cardinalità**: 1:1 (opzionale, un User può non avere un Locatario)
- **Cascade**: `CascadeType.ALL` su User

### One-to-Many / Many-to-One

#### Immobile → Contratto
- **Immobile**: `@OneToMany(mappedBy = "immobile")`
- **Contratto**: `@ManyToOne @JoinColumn(name = "immobile_id")`
- **Cascade**: `CascadeType.ALL` su Immobile

#### Immobile → Manutenzione
- **Immobile**: `@OneToMany(mappedBy = "immobile")`
- **Manutenzione**: `@ManyToOne @JoinColumn(name = "immobile_id")`
- **Cascade**: `CascadeType.ALL` su Immobile

#### Locatario → Contratto
- **Locatario**: `@OneToMany(mappedBy = "locatario")`
- **Contratto**: `@ManyToOne @JoinColumn(name = "locatario_id")`
- **Cascade**: `CascadeType.ALL` su Locatario

#### Locatario → Manutenzione
- **Locatario**: `@OneToMany(mappedBy = "locatario")`
- **Manutenzione**: `@ManyToOne @JoinColumn(name = "locatario_id")`
- **Cascade**: `CascadeType.ALL` su Locatario

#### Contratto → Rata
- **Contratto**: `@OneToMany(mappedBy = "contratto")`
- **Rata**: `@ManyToOne @JoinColumn(name = "contratto_id")`
- **Cascade**: `CascadeType.ALL` su Contratto

### Many-to-Many

#### User ↔ Role
- **Tabella Join**: `user_roles`
- **User**: `@ManyToMany @JoinTable`
- **Role**: `@ManyToMany(mappedBy = "roles")` (se definito)
- **Fetch**: `EAGER` su User (necessario per autorizzazione)
- **Cardinalità**: N:N (un utente può avere più ruoli)

## 📐 Inheritance Strategy: JOINED

L'ereditarietà per `Immobile` utilizza `InheritanceType.JOINED`:

```
immobile (tabella base)
├── id (PK)
├── indirizzo
├── citta
├── superficie
└── tipo

appartamento (tabella derivata)
├── immobile_id (PK, FK → immobile.id)
├── piano
└── numCamere

negozio (tabella derivata)
├── immobile_id (PK, FK → immobile.id)
├── vetrine
└── magazzinoMq

ufficio (tabella derivata)
├── immobile_id (PK, FK → immobile.id)
├── postiLavoro
└── saleRiunioni
```

**Vantaggi**:
- Normalizzazione: campi comuni in una tabella
- Flessibilità: aggiungere nuovi tipi senza modificare la base
- Performance: JOIN solo quando necessario

**Svantaggi**:
- JOIN necessari per recuperare dati completi
- Più complesso rispetto a SINGLE_TABLE

## 🔍 Fetch Strategies

### Lazy Loading (Default)
- **`@OneToMany`**: `FetchType.LAZY` (default)
- **`@ManyToMany`**: `FetchType.LAZY` (default)
- **Vantaggio**: Carica solo quando necessario
- **Svantaggio**: Possibile N+1 problem

### Eager Loading
- **`User.roles`**: `FetchType.EAGER` (necessario per Spring Security)
- **Vantaggio**: Carica subito
- **Svantaggio**: Possibile over-fetching

### Soluzioni N+1 Problem
- **`@EntityGraph`**: Per specificare fetch join
- **JPQL JOIN FETCH**: Query esplicite con join
- **Field Resolver GraphQL**: Carica relazioni solo quando richieste

## 📝 Enumerazioni

### TipoImmobile
```java
public enum TipoImmobile {
    APPARTAMENTO,
    NEGOZIO,
    UFFICIO
}
```

### FrequenzaRata
```java
public enum FrequenzaRata {
    MENSILE,      // 12 rate/anno
    BIMESTRALE,   // 6 rate/anno
    TRIMESTRALE,  // 4 rate/anno
    SEMESTRALE,   // 2 rate/anno
    ANNUALE       // 1 rata/anno
}
```

## 🔐 Vincoli e Validazioni

### Vincoli Database
- **UNIQUE**: `users.email`, `locatario.cf`, `user_roles` (composite PK)
- **NOT NULL**: Campi obbligatori
- **FOREIGN KEY**: Tutte le relazioni hanno FK con `ON DELETE CASCADE`
- **CHECK**: `immobile.tipo` deve essere uno dei valori enum

### Validazioni Java
- **`@NotBlank`**: String non vuote
- **`@NotNull`**: Campi obbligatori
- **`@Positive`**: Numeri positivi
- **`@Email`**: Formato email valido
- **`@Size`**: Lunghezza stringhe
- **`@Valid`**: Validazione nested objects

## 📊 Query Complesse

### Esempi di Query Custom

#### Trova immobili disponibili (senza contratti attivi)
```java
@Query("SELECT i FROM Immobile i WHERE i NOT IN " +
       "(SELECT c.immobile FROM Contratto c WHERE c.dataInizio <= :data " +
       "AND c.dataInizio + c.durataAnni >= :data)")
List<Immobile> findAvailableImmobili(@Param("data") LocalDate data);
```

#### Trova rate scadute non pagate
```java
@Query("SELECT r FROM Rata r WHERE r.dataScadenza < :oggi AND r.pagata = false")
List<Rata> findScaduteNonPagate(@Param("oggi") LocalDate oggi);
```

#### Statistiche contratti per locatario
```java
@Query("SELECT COUNT(c), SUM(c.canoneAnnuo) FROM Contratto c WHERE c.locatario.id = :locatarioId")
Object[] getContrattoStats(@Param("locatarioId") Long locatarioId);
```

---

Per dettagli su autenticazione e autorizzazione, consulta [AUTENTICAZIONE.md](./AUTENTICAZIONE.md).

