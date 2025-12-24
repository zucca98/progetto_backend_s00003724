# Architettura del Progetto

Questo documento descrive l'architettura del **Progetto_Backend**, i pattern utilizzati e la struttura del codice.

## 🏗️ Architettura Generale

Il progetto segue un'**architettura a strati (Layered Architecture)** tipica delle applicazioni Spring Boot:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Controllers REST + GraphQL Resolvers) │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Business Logic Layer            │
│              (Services)                 │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Data Access Layer               │
│          (Repositories)                 │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Database Layer                  │
│           (PostgreSQL)                  │
└─────────────────────────────────────────┘
```

## 📦 Struttura dei Package

### `com.epicode.Progetto_Backend`

#### **config/**
Contiene tutte le classi di configurazione Spring:

- **`SecurityConfig.java`**: Configurazione Spring Security, JWT, CORS, controlli di accesso
- **`GraphQLConfig.java`**: Configurazione GraphQL
- **`CorsConfig.java`**: Configurazione CORS
- **`SwaggerConfig.java`**: Configurazione Swagger/OpenAPI
- **`WebConfig.java`**: Configurazione web (interceptors, converters)
- **`JwtProperties.java`**: Properties per JWT (secret, expiration)
- **`CloudinaryProperties.java`**: Properties per Cloudinary
- **`MailgunProperties.java`**: Properties per Mailgun
- **`DataSeeder.java`**: Inizializzazione dati di test
- **`RateLimitingInterceptor.java`**: Rate limiting con Bucket4j

#### **controller/**
Contiene i **REST Controllers** che gestiscono le richieste HTTP:

- **`AuthController.java`**: Autenticazione (register, login)
- **`UserController.java`**: Gestione utenti
- **`ImmobileController.java`**: Gestione immobili
- **`ContrattoController.java`**: Gestione contratti
- **`LocatarioController.java`**: Gestione locatari
- **`RataController.java`**: Gestione rate
- **`ManutenzioneController.java`**: Gestione manutenzioni
- **`UploadController.java`**: Upload immagini profilo

**Pattern utilizzato**: Ogni controller:
- Espone endpoint REST standardizzati
- Utilizza `@PreAuthorize` per autorizzazione
- Implementa controlli di accesso aggiuntivi per LOCATARIO (verifica appartenenza risorse)
- Valida input con `@Valid`
- Restituisce DTO invece di entità
- Gestisce paginazione dove necessario

#### **dto/**
Contiene i **Data Transfer Objects** per il trasferimento dati:

- **`AuthResponseDTO.java`**: Risposta autenticazione (token, user info)
- **`LoginRequestDTO.java`**: Request login
- **`RegisterRequestDTO.java`**: Request registrazione
- **`UserUpdateDTO.java`**: Update utente
- **`ImmobileRequestDTO.java`**: Create/Update immobile
- **`ContrattoRequestDTO.java`**: Create/Update contratto
- **`LocatarioRequestDTO.java`**: Create/Update locatario
- **`RataRequestDTO.java`**: Create/Update rata
- **`ManutenzioneRequestDTO.java`**: Create/Update manutenzione
- **`PageResponse.java`**: Wrapper per paginazione

**Pattern utilizzato**: Separazione tra entità JPA (persistenza) e DTO (trasferimento dati) per:
- Nascondere dettagli implementativi
- Evitare problemi di serializzazione JSON
- Validare input in modo indipendente

#### **entity/**
Contiene le **entità JPA** che rappresentano il modello dati:

- **`User.java`**: Utente del sistema (implementa `UserDetails`)
- **`Role.java`**: Ruolo utente (ADMIN, MANAGER, LOCATARIO)
- **`Immobile.java`**: Classe base per immobili (ereditarietà JOINED)
- **`Appartamento.java`**: Sottoclasse di Immobile
- **`Negozio.java`**: Sottoclasse di Immobile
- **`Ufficio.java`**: Sottoclasse di Immobile
- **`Locatario.java`**: Locatario (One-to-One con User)
- **`Contratto.java`**: Contratto di affitto
- **`Rata.java`**: Rata di affitto
- **`Manutenzione.java`**: Manutenzione immobile
- **`TipoImmobile.java`**: Enum per tipo immobile
- **`FrequenzaRata.java`**: Enum per frequenza rate

**Pattern utilizzato**:
- **Inheritance Strategy**: `InheritanceType.JOINED` per immobili
- **Lombok**: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **JPA Annotations**: `@Entity`, `@Table`, `@OneToMany`, `@ManyToOne`, `@OneToOne`

#### **exception/**
Gestione centralizzata delle eccezioni:

- **`GlobalExceptionHandler.java`**: `@RestControllerAdvice` per gestire tutte le eccezioni
- **`EntityNotFoundException.java`**: Eccezione quando un'entità non esiste
- **`ValidationException.java`**: Eccezione per errori di validazione
- **`BusinessException.java`**: Eccezione per errori di business logic
- **`ErrorResponse.java`**: DTO per risposte di errore strutturate

**Pattern utilizzato**: **Global Exception Handling** per:
- Risposte di errore consistenti
- Logging centralizzato
- Separazione tra errori tecnici e business

#### **graphql/**
Implementazione GraphQL:

- **`QueryResolver.java`**: Risolve tutte le query GraphQL
- **`MutationResolver.java`**: Risolve tutte le mutation GraphQL
- **`FieldResolver.java`**: Risolve campi lazy (relazioni)
- **`input/`**: Input types GraphQL (ImmobileInput, ContrattoInput, etc.)

**Pattern utilizzato**:
- **Resolver Pattern**: Separazione query/mutation/field resolver
- **Schema-First**: Schema definito in `schema.graphqls`
- **Authorization**: `@PreAuthorize` anche su resolver GraphQL

#### **repository/**
Interfacce **Spring Data JPA Repository**:

- **`UserRepository.java`**: Query per User
- **`RoleRepository.java`**: Query per Role
- **`ImmobileRepository.java`**: Query per Immobile
- **`AppartamentoRepository.java`**: Query per Appartamento
- **`NegozioRepository.java`**: Query per Negozio
- **`UfficioRepository.java`**: Query per Ufficio
- **`LocatarioRepository.java`**: Query per Locatario
- **`ContrattoRepository.java`**: Query per Contratto
- **`RataRepository.java`**: Query per Rata
- **`ManutenzioneRepository.java`**: Query per Manutenzione

**Pattern utilizzato**:
- **Repository Pattern**: Astrazione accesso dati
- **Spring Data JPA**: Query methods, JPQL, Native SQL
- **Custom Queries**: Query complesse con `@Query`

#### **security/**
Implementazione sicurezza:

- **`SecurityConfig.java`**: Configurazione Spring Security
- **`JwtTokenProvider.java`**: Generazione e validazione token JWT
- **`JwtAuthenticationFilter.java`**: Filter per estrarre token da header

**Pattern utilizzato**:
- **JWT Authentication**: Token-based authentication
- **Filter Chain**: `JwtAuthenticationFilter` prima di `UsernamePasswordAuthenticationFilter`
- **Method Security**: `@PreAuthorize` per autorizzazione a livello metodo

#### **service/**
Logiche di business:

- **`AuthService.java`**: Logica autenticazione e registrazione
- **`UserService.java`**: Logica gestione utenti
- **`ImmobileService.java`**: Logica gestione immobili
- **`ContrattoService.java`**: Logica gestione contratti
- **`LocatarioService.java`**: Logica gestione locatari
- **`RataService.java`**: Logica gestione rate
- **`ManutenzioneService.java`**: Logica gestione manutenzioni
- **`CloudinaryService.java`**: Integrazione Cloudinary
- **`MailgunService.java`**: Integrazione Mailgun
- **`CustomUserDetailsService.java`**: Caricamento UserDetails per Spring Security

**Pattern utilizzato**:
- **Service Layer Pattern**: Business logic separata dai controller
- **Dependency Injection**: `@Autowired` o `@RequiredArgsConstructor`
- **Transactional**: `@Transactional` per operazioni database

#### **util/**
Classi di utilità:

- **`DebugLogger.java`**: Logger personalizzato per debug

## 🔄 Flusso di una Richiesta

### Richiesta REST

```
1. HTTP Request
   ↓
2. SecurityFilterChain (SecurityConfig)
   ↓
3. JwtAuthenticationFilter (estrazione token)
   ↓
4. Controller (@RestController)
   ↓
5. Validazione (@Valid)
   ↓
6. Autorizzazione (@PreAuthorize)
   ↓
7. Service (business logic)
   ↓
8. Repository (accesso dati)
   ↓
9. Database (PostgreSQL)
   ↓
10. Response (DTO serializzato in JSON)
```

### Richiesta GraphQL

```
1. HTTP POST /graphql
   ↓
2. SecurityFilterChain (autenticazione)
   ↓
3. GraphQL Engine (Spring GraphQL)
   ↓
4. Query/Mutation Resolver
   ↓
5. Autorizzazione (@PreAuthorize)
   ↓
6. Service (business logic)
   ↓
7. Repository (accesso dati)
   ↓
8. Field Resolver (se necessario, per relazioni lazy)
   ↓
9. Database (PostgreSQL)
   ↓
10. Response (JSON GraphQL)
```

## 🎯 Design Patterns Utilizzati

### 1. **Layered Architecture**
Separazione chiara tra Presentation, Business, Data Access.

### 2. **Repository Pattern**
Astrazione accesso dati tramite interfacce Spring Data JPA.

### 3. **Service Layer Pattern**
Business logic isolata nei service, controller solo per HTTP.

### 4. **DTO Pattern**
Separazione tra entità JPA e oggetti di trasferimento dati.

### 5. **Builder Pattern**
Utilizzato tramite Lombok `@Builder` per costruzione oggetti complessi.

### 6. **Strategy Pattern**
Per gestione diversi tipi di immobili (Appartamento, Negozio, Ufficio).

### 7. **Template Method Pattern**
Spring Data JPA Repository fornisce template per query comuni.

### 8. **Filter Pattern**
`JwtAuthenticationFilter` per autenticazione JWT.

### 9. **Global Exception Handler**
`@RestControllerAdvice` per gestione centralizzata errori.

## 🔐 Sicurezza

### Autenticazione
- **JWT Token**: Token firmato con secret, validità 24h
- **Password Encoding**: BCrypt con salt automatico
- **Stateless**: Nessuna sessione server-side

### Autorizzazione
- **Role-Based Access Control (RBAC)**: 3 ruoli (ADMIN, MANAGER, LOCATARIO)
- **Method-Level Security**: `@PreAuthorize` su controller e resolver
- **Endpoint Protection**: Tutti gli endpoint (tranne `/api/auth/**`) richiedono autenticazione
- **Access Control**: Controlli di appartenenza per LOCATARIO negli endpoint "by id" per prevenire Broken Access Control
  - Verifica che contratti, rate e manutenzioni appartengano al locatario corrente
  - Restituisce 403 Forbidden se il locatario tenta di accedere a risorse di altri locatari

### CORS
Configurato in `CorsConfig.java` per permettere richieste da frontend specifici.

## 📊 Gestione Dati

### Transazioni
- **`@Transactional`**: Su metodi service che modificano dati
- **Propagation**: Default (REQUIRED)
- **Isolation**: Default (READ_COMMITTED)

### Lazy Loading
- **FetchType.LAZY**: Per relazioni `@OneToMany` e `@ManyToMany`
- **FetchType.EAGER**: Solo per `User.roles` (necessario per autorizzazione)
- **N+1 Problem**: Risolto con `@EntityGraph` o join fetch dove necessario

### Paginazione
- **Spring Data Pageable**: Per liste di entità
- **PageResponse**: DTO wrapper per risposte paginate

## 🧪 Testing

Struttura test in `src/test/java/`:
- **Unit Tests**: Per service e repository
- **Integration Tests**: Per controller REST e GraphQL
- **Security Tests**: Per autenticazione e autorizzazione

## 📝 Best Practices Implementate

✅ **Separation of Concerns**: Ogni layer ha responsabilità chiare  
✅ **DRY (Don't Repeat Yourself)**: Lombok, utility classes  
✅ **SOLID Principles**: Single Responsibility, Dependency Inversion  
✅ **Validation**: `@Valid` su tutti gli input  
✅ **Error Handling**: Global exception handler  
✅ **Logging**: Logger strutturato con SLF4J  
✅ **Documentation**: Swagger per API REST, Schema per GraphQL  
✅ **Configuration Externalization**: Properties in `application.properties` e `env.properties`

---

Per dettagli su entità e relazioni, consulta [ENTITA_E_RELAZIONI.md](./ENTITA_E_RELAZIONI.md).

