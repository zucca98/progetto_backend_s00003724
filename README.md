# Documentazione Progetto_Backend

Benvenuto nella documentazione completa del **Progetto_Backend**, un sistema di gestione per una cooperativa immobiliare sviluppato con **Spring Boot 3.5.9** e **PostgreSQL**.

## 📚 Indice della Documentazione

Questa cartella contiene una documentazione completa e strutturata del progetto:

1. **[README.md](./README.md)** (questo file) - Panoramica generale del progetto
2. **[ARCHITETTURA.md](./docs/ARCHITETTURA.md)** - Architettura del sistema, pattern e struttura del codice
3. **[ENTITA_E_RELAZIONI.md](./docs/ENTITA_E_RELAZIONI.md)** - Modello dati, entità JPA e relazioni
4. **[AUTENTICAZIONE.md](./docs/AUTENTICAZIONE.md)** - Sistema di autenticazione JWT e autorizzazione basata su ruoli
5. **[REST_API.md](./docs/REST_API.md)** - Documentazione completa delle API REST
6. **[GRAPHQL.md](./docs/GRAPHQL.md)** - Schema GraphQL, query e mutation
7. **[SERVIZI_E_LOGICHE.md](./docs/SERVIZI_E_LOGICHE.md)** - Logiche di business e servizi
8. **[CONFIGURAZIONE.md](./docs/CONFIGURAZIONE.md)** - Configurazione, properties e setup
9. **[INTEGRAZIONI_ESTERNE.md](./docs/INTEGRAZIONI_ESTERNE.md)** - Integrazioni con API esterne (Cloudinary, Mailgun)
10. **[OTTIMIZZAZIONI_QUERY.md](./docs/OTTIMIZZAZIONI_QUERY.md)** - Ottimizzazioni delle query, prevenzione N+1 e test di performance

## 🎯 Panoramica del Progetto

### Dominio Applicativo
Il sistema gestisce una **cooperativa immobiliare** che si occupa di:
- Gestione di immobili (appartamenti, negozi, uffici)
- Gestione di contratti di affitto
- Gestione di locatari
- Gestione di rate di affitto
- Gestione di manutenzioni

### Stack Tecnologico

- **Framework**: Spring Boot 3.5.9
- **Linguaggio**: Java 21
- **Database**: PostgreSQL
- **ORM**: Hibernate / JPA
- **Sicurezza**: Spring Security + JWT
- **API**: REST + GraphQL
- **Documentazione API**: Swagger/OpenAPI
- **Build Tool**: Maven
- **Lombok**: Per ridurre boilerplate code

### Caratteristiche Principali

✅ **Gestione Utenti Completa**
- Registrazione e autenticazione
- Profili utente con immagine aggiornabile
- Sistema di ruoli (ADMIN, MANAGER, LOCATARIO)

✅ **Modello Dati Complesso**
- 8+ entità con relazioni significative
- Ereditarietà JPA (`InheritanceType.JOINED`) per tipi di immobili
- Relazioni One-to-Many, Many-to-One, One-to-One

✅ **API REST Complete**
- CRUD completo per tutte le entità
- Paginazione e filtri
- Validazione dati
- Gestione errori strutturata

✅ **API GraphQL**
- Schema GraphQL completo
- Query e Mutation per tutte le entità
- Field Resolver per relazioni lazy

✅ **Sicurezza**
- Autenticazione JWT
- Autorizzazione basata su ruoli (`@PreAuthorize`)
- Password encoding con BCrypt
- Controlli di accesso per prevenire Broken Access Control
- Verifica appartenenza risorse per utenti LOCATARIO

✅ **Integrazioni Esterne**
- **Cloudinary**: Upload e gestione immagini profilo
- **Mailgun**: Invio email di notifica

✅ **Best Practices**
- Separazione delle responsabilità (Controller → Service → Repository)
- DTO per trasferimento dati
- Exception handling globale
- Logging strutturato
- Rate limiting
- Ottimizzazioni query (EntityGraph, JOIN FETCH) per prevenire problemi N+1
- Security best practices: controlli di accesso granulari per prevenire accesso non autorizzato

## 📁 Struttura del Progetto

```
Progetto_Backend/
├── src/
│   ├── main/
│   │   ├── java/com/epicode/Progetto_Backend/
│   │   │   ├── config/          # Configurazioni (Security, CORS, GraphQL, etc.)
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # Entità JPA
│   │   │   ├── exception/       # Eccezioni custom e handler
│   │   │   ├── graphql/         # GraphQL Resolvers e Input Types
│   │   │   ├── repository/      # Repository JPA
│   │   │   ├── security/        # JWT e Security Config
│   │   │   ├── service/         # Business Logic
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── graphql/schema.graphqls
│   │       └── init-data.sql
│   └── test/                    # Test unitari e di integrazione
├── postman/                     # Collection Postman per test API
├── docs/                        # Documentazione (questa cartella)
└── pom.xml                      # Configurazione Maven
```

## 🚀 Quick Start

Per iniziare a utilizzare il progetto:

1. **Configurazione Database**: Crea un database PostgreSQL e configura le credenziali in `env.properties`
2. **Configurazione API Esterne**: Configura Cloudinary e Mailgun in `env.properties`
3. **Avvio Applicazione**: Esegui `mvn spring-boot:run` o avvia `AlexApplication.java`
4. **Documentazione API**: Accedi a Swagger UI su `http://localhost:8080/swagger-ui.html`
5. **GraphQL Playground**: Accedi a GraphiQL su `http://localhost:8080/graphiql`

Per maggiori dettagli, consulta [CONFIGURAZIONE.md](./docs/CONFIGURAZIONE.md).

## 📮 Utilizzo Collection Postman

Il progetto include una **collection Postman completa** con tutti gli endpoint REST e GraphQL per testare facilmente l'API.

### Importare la Collection

1. **Apri Postman**
2. **Importa Collection**: 
   - Clicca su `Import` (in alto a sinistra)
   - Seleziona il file `postman/backend-collection.postman_collection.json`
3. **Importa Environment**:
   - Clicca su `Import` di nuovo
   - Seleziona il file `postman/backend-env.postman_environment.json`
   - Seleziona l'environment importato dal menu a tendina in alto a destra

### Configurare le Variabili

L'environment include variabili pre-configurate che puoi modificare:

- **`baseUrl`**: URL base dell'API (default: `http://localhost:8080`)
- **Token**: I token vengono salvati automaticamente dopo il login:
  - `adminToken`, `managerToken`, `locatarioToken`, `accessToken`
- **Credenziali di default**:
  - `adminEmail`: `admin@cooperativa.it` / `adminPassword`: `admin123`
  - `managerEmail`: `manager@cooperativa.it` / `managerPassword`: `manager123`
  - `locatarioEmail`: `giuseppe.verdi@email.it` / `locatarioPassword`: `locatario123`
- **ID Risorse**: Vengono popolati automaticamente dopo le operazioni CRUD:
  - `userId`, `immobileId`, `locatarioId`, `contrattoId`, `rataId`, `manutenzioneId`

### Utilizzare la Collection

1. **Autenticazione**:
   - Vai alla cartella `01 - Autenticazione`
   - Esegui `Register` o `Login` per ottenere un token
   - Il token viene salvato automaticamente nella variabile corrispondente

2. **Testare gli Endpoint REST**:
   - Le richieste sono organizzate per risorsa (Utenti, Immobili, Contratti, ecc.)
   - Usa le variabili `{{baseUrl}}`, `{{adminToken}}`, ecc. nelle richieste
   - I Test Script salvano automaticamente gli ID nelle variabili d'ambiente

3. **Testare GraphQL**:
   - Vai alla cartella `09 - GraphQL`
   - Le query e mutation sono organizzate in sottocartelle
   - Usa il token appropriato nell'header `Authorization: Bearer {{adminToken}}`

4. **Gestione Token**:
   - Dopo ogni login, il token viene salvato nella variabile corrispondente
   - I token sono validi per 24 ore (configurabile in `application.properties`)
   - Puoi usare `{{adminToken}}`, `{{managerToken}}`, o `{{locatarioToken}}` nelle richieste

Per maggiori dettagli sulla collection, consulta [CONFIGURAZIONE.md](./docs/CONFIGURAZIONE.md#-utilizzo-collection-postman).

## 🔐 Ruoli e Permessi

Il sistema prevede **3 ruoli principali**:

- **ADMIN**: Accesso completo a tutte le funzionalità
- **MANAGER**: Gestione di immobili, contratti, locatari, rate e manutenzioni
- **LOCATARIO**: Accesso limitato ai propri dati e contratti
  - Gli endpoint "by id" verificano che la risorsa appartenga al locatario corrente
  - Tentativi di accesso a risorse di altri locatari restituiscono **403 Forbidden**

Per dettagli completi, consulta [AUTENTICAZIONE.md](./docs/AUTENTICAZIONE.md) e [REST_API.md](./docs/REST_API.md).

## 📖 Come Usare Questa Documentazione

Questa documentazione è pensata per essere consultata in modo modulare:

- **Sviluppatori nuovi al progetto**: Inizia da [ARCHITETTURA.md](./docs/ARCHITETTURA.md) e [ENTITA_E_RELAZIONI.md](./docs/ENTITA_E_RELAZIONI.md)
- **Per implementare nuove funzionalità**: Consulta [SERVIZI_E_LOGICHE.md](./docs/SERVIZI_E_LOGICHE.md) e [REST_API.md](./docs/REST_API.md)
- **Per configurare l'ambiente**: Vedi [CONFIGURAZIONE.md](./docs/CONFIGURAZIONE.md)
- **Per integrare API esterne**: Vedi [INTEGRAZIONI_ESTERNE.md](./docs/INTEGRAZIONI_ESTERNE.md)
- **Per ottimizzare le query**: Vedi [OTTIMIZZAZIONI_QUERY.md](.//docs/OTTIMIZZAZIONI_QUERY.md)

## 📝 Note Importanti

- Il progetto utilizza **Lombok** per ridurre il boilerplate (getter, setter, builder, etc.)
- Le password sono hashate con **BCrypt**
- I token JWT hanno una validità di **24 ore** (configurabile)
- Il database viene inizializzato automaticamente tramite `init-data.sql` e `DataSeeder`
- La documentazione Swagger è disponibile solo in ambiente di sviluppo
- **Sicurezza**: Gli endpoint "by id" per LOCATARIO includono controlli di appartenenza per prevenire accesso non autorizzato a risorse di altri locatari

## 🔗 Link Utili

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **GraphiQL**: `http://localhost:8080/graphiql`
- **API Docs (JSON)**: `http://localhost:8080/api-docs`
- **Health Check**: `http://localhost:8080/actuator/health`

---

**Versione Documentazione**: 1.1  
**Ultimo Aggiornamento**: Dicembre 2024

### 🔒 Aggiornamenti Sicurezza (v1.1)

- ✅ Implementati controlli di accesso per prevenire Broken Access Control
- ✅ Verifica appartenenza risorse per utenti LOCATARIO negli endpoint "by id"
- ✅ Endpoint protetti: `/api/contratti/{id}`, `/api/rate/{id}`, `/api/rate/contratto/{contrattoId}`, `/api/manutenzioni/{id}`

