# Configurazione e Setup

Questo documento descrive la **configurazione** del progetto, le **properties** e le **istruzioni di setup**.

## 📋 Requisiti

### Software Necessario
- **Java**: 21 o superiore
- **Maven**: 3.8+ (o utilizza `mvnw` incluso)
- **PostgreSQL**: 14+ (o versione compatibile)
- **IDE**: IntelliJ IDEA, Eclipse, VS Code (opzionale)

### Account Esterni
- **Cloudinary**: Account per upload immagini
- **Mailgun**: Account per invio email (opzionale per sviluppo)

## 🔧 Setup Iniziale

### 1. Clonare il Repository

```bash
git clone <repository-url>
cd Progetto_Backend
```

### 2. Configurare Database PostgreSQL

#### Creare Database
```sql
CREATE DATABASE cooperativa_immobiliare;
```

#### Configurare Credenziali
Crea file `env.properties` nella root del progetto (vedi `env.properties.example`):

```properties
# Database
db.url=jdbc:postgresql://localhost:5432/cooperativa_immobiliare
db.username=postgres
db.password=your_password

# JWT
jwt.secret=your-super-secret-key-min-256-bits-for-hmac-sha256

# Cloudinary
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret

# Mailgun
mailgun.api-key=your_mailgun_api_key
mailgun.domain=your_mailgun_domain
mailgun.from-email=noreply@yourdomain.com
```

**⚠️ IMPORTANTE**: Non committare `env.properties` nel repository (è già in `.gitignore`).

### 3. Inizializzare Database

Il database viene inizializzato automaticamente tramite:
- **`init-data.sql`**: Script SQL per creazione tabelle e dati iniziali
- **`DataSeeder.java`**: Classe Java che popola dati di test

**Configurazione JPA**:
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Note**:
- `ddl-auto=update` crea/aggiorna automaticamente le tabelle
- In produzione, usare `ddl-auto=validate` e migrazioni Flyway/Liquibase

### 4. Avviare l'Applicazione

#### Con Maven Wrapper
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### Con Maven Installato
```bash
mvn spring-boot:run
```

#### Con IDE
Esegui `AlexApplication.java` come Java Application.

### 5. Verificare Avvio

L'applicazione dovrebbe essere disponibile su:
- **Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **GraphiQL**: `http://localhost:8080/graphiql`
- **Health Check**: `http://localhost:8080/actuator/health`

### 6. Utilizzare la Collection Postman (Opzionale ma Consigliato)

Il progetto include una **collection Postman completa** con tutti gli endpoint REST e GraphQL per testare facilmente l'API.

#### Importare la Collection

1. **Apri Postman** (installalo da [postman.com](https://www.postman.com/downloads/) se non lo hai)

2. **Importa Collection**:
   - Clicca sul pulsante `Import` (in alto a sinistra)
   - Seleziona il file `postman/backend-collection.postman_collection.json`
   - La collection verrà aggiunta alla tua workspace

3. **Importa Environment**:
   - Clicca su `Import` di nuovo
   - Seleziona il file `postman/backend-env.postman_environment.json`
   - Seleziona l'environment importato dal menu a tendina in alto a destra ("No Environment" → "Cooperativa Immobiliare - Environment")

#### Variabili d'Ambiente Disponibili

L'environment include le seguenti variabili pre-configurate:

**URL Base**:
- `baseUrl`: URL base dell'API (default: `http://localhost:8080`)

**Token di Autenticazione** (popolati automaticamente dopo il login):
- `adminToken`: Token JWT per utente ADMIN
- `managerToken`: Token JWT per utente MANAGER
- `locatarioToken`: Token JWT per utente LOCATARIO
- `accessToken`: Token generico (usato come fallback)

**Credenziali di Default** (utenti creati da DataSeeder):
- `adminEmail`: `admin@cooperativa.it`
- `adminPassword`: `admin123`
- `managerEmail`: `manager@cooperativa.it`
- `managerPassword`: `manager123`
- `locatarioEmail`: `giuseppe.verdi@email.it`
- `locatarioPassword`: `locatario123`

**ID Risorse** (popolati automaticamente dopo operazioni CRUD):
- `userId`, `adminUserId`, `managerUserId`, `locatarioUserId`
- `immobileId`, `locatarioId`, `contrattoId`, `rataId`, `manutenzioneId`

#### Come Utilizzare la Collection

1. **Autenticarsi**:
   - Vai alla cartella `01 - Autenticazione`
   - Esegui prima `Register` (opzionale) o `Login` con le credenziali di default
   - Il token viene salvato automaticamente nella variabile corrispondente tramite Test Script

2. **Testare Endpoint REST**:
   - Naviga nelle cartelle organizzate per risorsa:
     - `02 - Utenti`
     - `03 - Immobili`
     - `04 - Contratti`
     - `05 - Locatari`
     - `06 - Rate`
     - `07 - Manutenzioni`
     - `08 - Upload`
   - Usa le variabili nelle richieste: `{{baseUrl}}`, `{{adminToken}}`, `{{immobileId}}`, ecc.
   - I Test Script nelle richieste salvano automaticamente gli ID nelle variabili

3. **Testare GraphQL**:
   - Vai alla cartella `09 - GraphQL`
   - Le query e mutation sono organizzate in sottocartelle:
     - `GraphQL - Queries`: Tutte le query GraphQL disponibili
     - `GraphQL - Mutations`: Tutte le mutation GraphQL disponibili
   - Usa il token appropriato nell'header: `Authorization: Bearer {{adminToken}}`
   - Esempio body per una query:
     ```json
     {
       "query": "query { immobili { id indirizzo citta superficie } }"
     }
     ```

4. **Gestire i Token**:
   - Dopo ogni login, il token viene salvato automaticamente nella variabile corretta
   - I token sono validi per 24 ore (configurabile in `application.properties`)
   - Per cambiare utente, esegui un nuovo login con credenziali diverse
   - Puoi vedere/modificare i token nell'environment: clicca sull'icona dell'occhio nell'environment

5. **Modificare le Variabili**:
   - Se l'applicazione è su una porta diversa, modifica `baseUrl` nell'environment
   - Se usi credenziali diverse, modifica le variabili email/password prima di fare login
   - Gli ID vengono aggiornati automaticamente, ma puoi modificarli manualmente se necessario

#### Struttura della Collection

La collection è organizzata logicamente:

```
📁 01 - Autenticazione
   ├── Register
   └── Login

📁 02 - Utenti
   ├── Get All Users (Admin)
   ├── Get User by ID
   ├── Get Current User (/me)
   └── Update User

📁 03 - Immobili
   ├── Create Immobile (Appartamento)
   ├── Create Immobile (Negozio)
   ├── Create Immobile (Ufficio)
   ├── Get All Immobili
   ├── Get Immobile by ID
   └── Update/Delete Immobile

📁 04 - Contratti
   ├── Create Contratto
   ├── Get All Contratti
   ├── Get Contratto by ID
   └── Get My Contratti (/me)

📁 05 - Locatari
📁 06 - Rate
📁 07 - Manutenzioni
📁 08 - Upload
📁 09 - GraphQL
   ├── GraphQL - Queries
   └── GraphQL - Mutations
```

#### Note Importanti

- **Ordine delle Operazioni**: Alcune richieste dipendono da altre (es. per creare un contratto serve un locatario e un immobile)
- **Autorizzazioni**: Alcuni endpoint richiedono ruoli specifici (es. `GET /api/users` richiede `ROLE_ADMIN`)
- **Test Script**: Molte richieste includono script che salvano automaticamente dati utili (ID, token) nelle variabili
- **Variabili Dinamiche**: Gli ID vengono aggiornati automaticamente quando crei nuove risorse, ma puoi modificarli manualmente

## 📝 File di Configurazione

### application.properties

File principale in `src/main/resources/application.properties`.

#### Database Configuration
```properties
spring.datasource.url=${db.url}
spring.datasource.username=${db.username}
spring.datasource.password=${db.password}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.postgresql.dialect.PostgreSQLDialect
```

#### JWT Configuration
```properties
jwt.secret=${jwt.secret}
jwt.expiration=86400000  # 24 ore in millisecondi
```

#### Cloudinary Configuration
```properties
cloudinary.cloud-name=${cloudinary.cloud-name}
cloudinary.api-key=${cloudinary.api-key}
cloudinary.api-secret=${cloudinary.api-secret}
```

#### Mailgun Configuration
```properties
mailgun.api-key=${mailgun.api-key}
mailgun.domain=${mailgun.domain}
mailgun.from-email=${mailgun.from-email}
```

#### GraphQL Configuration
```properties
spring.graphql.schema.introspection.enable=true
spring.graphql.http.path=/graphql
spring.graphql.graphiql.enabled=true
```

#### Swagger Configuration
```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```

#### File Upload Configuration
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

#### CORS Configuration
```properties
cors.allowed-origins=http://localhost:3000,http://localhost:5173,http://localhost:4200
cors.allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true
```

#### Logging Configuration
```properties
logging.level.root=INFO
logging.level.com.epicode.Progetto_Backend=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### env.properties

File per variabili d'ambiente sensibili (non committato).

**Template** (`env.properties.example`):
```properties
# Database
db.url=jdbc:postgresql://localhost:5432/cooperativa_immobiliare
db.username=postgres
db.password=password

# JWT Secret (minimo 256 bit per HMAC-SHA256)
jwt.secret=your-super-secret-key-min-256-bits-for-hmac-sha256

# Cloudinary
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret

# Mailgun
mailgun.api-key=your_mailgun_api_key
mailgun.domain=your_mailgun_domain
mailgun.from-email=noreply@yourdomain.com
```

**Generazione JWT Secret**:
```bash
# Linux/Mac
openssl rand -base64 32

# Online
# Usa un generatore di stringhe casuali (minimo 32 caratteri)
```

## 🔐 Configurazione Sicurezza

### JWT Secret

Il JWT secret deve essere:
- **Lunghezza minima**: 256 bit (32 caratteri) per HMAC-SHA256
- **Sicurezza**: Stringa casuale e segreta
- **Ambiente**: Diverso per sviluppo e produzione

### Password Encoding

Le password vengono hashate con **BCrypt**:
- **Cost Factor**: 10 (default Spring Security)
- **Salt**: Automatico (unico per ogni password)

## 🌍 Profili Spring

### Development (default)
```properties
spring.profiles.active=dev
```

**Caratteristiche**:
- Logging dettagliato
- SQL visibile nei log
- GraphiQL abilitato
- Swagger abilitato

### Production

Crea `application-prod.properties`:

```properties
spring.profiles.active=prod

# Database
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Logging
logging.level.root=WARN
logging.level.com.epicode.Progetto_Backend=INFO

# GraphQL
spring.graphql.graphiql.enabled=false

# Swagger
springdoc.swagger-ui.enabled=false

# CORS (specifica domini produzione)
cors.allowed-origins=https://yourdomain.com
```

**Attivazione**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 📊 Configurazione Database

### Schema Auto-Update

**Sviluppo**:
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Produzione**:
```properties
spring.jpa.hibernate.ddl-auto=validate
```

**Note**:
- `update`: Crea/aggiorna tabelle automaticamente
- `validate`: Solo valida schema esistente
- In produzione, usare migrazioni (Flyway/Liquibase)

### Connection Pool

Spring Boot usa **HikariCP** come connection pool (default):
- **Max Pool Size**: 10 (default)
- **Min Idle**: 5 (default)
- **Connection Timeout**: 30s (default)

Configurazione personalizzata (opzionale):
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=20000
```

## 🔧 Configurazione Cloudinary

### Setup Account

1. Registrati su [cloudinary.com](https://cloudinary.com)
2. Ottieni:
   - **Cloud Name**: Nome del tuo cloud
   - **API Key**: Chiave API
   - **API Secret**: Secret API

### Configurazione

In `env.properties`:
```properties
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
```

### Folder Structure

Le immagini vengono caricate in:
```
profile_images/
  ├── {auto-generated-id}.jpg
  └── ...
```

## 📧 Configurazione Mailgun

### Setup Account

1. Registrati su [mailgun.com](https://mailgun.com)
2. Verifica dominio (o usa sandbox per test)
3. Ottieni:
   - **API Key**: Chiave API
   - **Domain**: Dominio verificato
   - **From Email**: Email mittente

### Configurazione

In `env.properties`:
```properties
mailgun.api-key=your_mailgun_api_key
mailgun.domain=your_mailgun_domain
mailgun.from-email=noreply@yourdomain.com
```

### Sandbox (Sviluppo)

Per sviluppo, puoi usare il sandbox Mailgun:
- **Domain**: `sandbox{xxx}.mailgun.org`
- **Limite**: 300 email/giorno
- **Destinatari**: Solo email verificate

## 🧪 Configurazione Testing

### application-test.properties

File in `src/test/resources/application-test.properties`:

```properties
# Database H2 in-memory per test
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Disabilita servizi esterni
cloudinary.enabled=false
mailgun.enabled=false
```

**Note**:
- Usa H2 in-memory database per test
- Disabilita Cloudinary e Mailgun (mock nei test)

## 🚀 Deploy in Produzione

### Checklist Pre-Deploy

- [ ] Cambiare `spring.profiles.active=prod`
- [ ] Configurare `ddl-auto=validate`
- [ ] Disabilitare GraphiQL
- [ ] Disabilitare Swagger
- [ ] Configurare CORS per domini produzione
- [ ] Configurare logging appropriato
- [ ] Usare JWT secret forte e unico
- [ ] Configurare connection pool appropriato
- [ ] Verificare variabili d'ambiente

### Variabili d'Ambiente

In produzione, usa variabili d'ambiente invece di `env.properties`:

```bash
export DB_URL=jdbc:postgresql://prod-db:5432/cooperativa_immobiliare
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET=super-secret-key-256-bits
# ...
```

### Docker (Opzionale)

Esempio `Dockerfile`:
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/Progetto_Backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 📝 Troubleshooting

### Database Connection Error

**Errore**: `Connection refused` o `Authentication failed`

**Soluzione**:
1. Verifica che PostgreSQL sia avviato
2. Verifica credenziali in `env.properties`
3. Verifica che il database esista
4. Verifica firewall/network

### JWT Secret Error

**Errore**: `Invalid key length`

**Soluzione**:
- JWT secret deve essere minimo 256 bit (32 caratteri)
- Genera nuovo secret: `openssl rand -base64 32`

### Cloudinary Upload Error

**Errore**: `Invalid API credentials`

**Soluzione**:
1. Verifica credenziali in `env.properties`
2. Verifica che l'account Cloudinary sia attivo
3. Verifica limiti quota

### Mailgun Send Error

**Errore**: `Failed to send email`

**Soluzione**:
1. Verifica credenziali in `env.properties`
2. Verifica dominio verificato
3. Per sandbox, verifica che destinatario sia autorizzato

---

Per dettagli su integrazioni esterne, consulta [INTEGRAZIONI_ESTERNE.md](./INTEGRAZIONI_ESTERNE.md).

