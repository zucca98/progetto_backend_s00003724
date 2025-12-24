# REST API Documentation

Questo documento descrive tutte le **API REST** esposte dal sistema, organizzate per risorsa.

## 📋 Indice

- [Autenticazione](#autenticazione)
- [Utenti](#utenti)
- [Immobili](#immobili)
- [Contratti](#contratti)
- [Locatari](#locatari)
- [Rate](#rate)
- [Manutenzioni](#manutenzioni)
- [Upload](#upload)

## 🔐 Autenticazione

### POST /api/auth/register
Registra un nuovo utente.

**Autorizzazione**: Pubblico (nessuna autenticazione richiesta)

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nome": "Mario",
  "cognome": "Rossi"
}
```

**Response** (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "nome": "Mario",
  "cognome": "Rossi"
}
```

**Validazioni**:
- `email`: @Email, @NotBlank
- `password`: @NotBlank, min 6 caratteri
- `nome`: @NotBlank
- `cognome`: @NotBlank

---

### POST /api/auth/login
Autentica un utente esistente.

**Autorizzazione**: Pubblico

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "nome": "Mario",
  "cognome": "Rossi"
}
```

**Errori**:
- **401**: Credenziali non valide

---

## 👤 Utenti

### GET /api/users
Ottiene tutti gli utenti (paginated).

**Autorizzazione**: `ROLE_ADMIN`

**Query Parameters**:
- `page` (default: 0)
- `size` (default: 20)
- `sort` (default: "id")

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "email": "user@example.com",
      "nome": "Mario",
      "cognome": "Rossi",
      "profileImage": "https://cloudinary.com/...",
      "registrationDate": "2024-01-01",
      "enabled": true,
      "roles": [
        {"id": 1, "name": "ROLE_ADMIN"}
      ]
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### GET /api/users/{id}
Ottiene un utente per ID.

**Autorizzazione**: `ROLE_ADMIN`

**Response** (200 OK):
```json
{
  "id": 1,
  "email": "user@example.com",
  "nome": "Mario",
  "cognome": "Rossi",
  "profileImage": "https://cloudinary.com/...",
  "registrationDate": "2024-01-01",
  "enabled": true,
  "roles": [...]
}
```

---

### GET /api/users/me
Ottiene l'utente corrente (autenticato).

**Autorizzazione**: `isAuthenticated()`

**Response** (200 OK):
```json
{
  "id": 1,
  "email": "user@example.com",
  "nome": "Mario",
  "cognome": "Rossi",
  ...
}
```

---

### PUT /api/users/me
Aggiorna l'utente corrente.

**Autorizzazione**: `isAuthenticated()`

**Request Body**:
```json
{
  "nome": "Mario",
  "cognome": "Rossi",
  "profileImage": "https://cloudinary.com/..."
}
```

**Response** (200 OK): Utente aggiornato

---

### PUT /api/users/{id}
Aggiorna un utente (solo ADMIN).

**Autorizzazione**: `ROLE_ADMIN`

**Request Body**: Stesso formato di `PUT /api/users/me`

---

### PUT /api/users/{id}/roles
Aggiorna i ruoli di un utente.

**Autorizzazione**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "roles": ["ROLE_MANAGER", "ROLE_LOCATARIO"]
}
```

**Response** (200 OK): Utente con ruoli aggiornati

---

### DELETE /api/users/{id}
Elimina un utente.

**Autorizzazione**: `ROLE_ADMIN`

**Response** (204 No Content)

---

## 🏠 Immobili

### GET /api/immobili
Ottiene tutti gli immobili (paginated).

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_LOCATARIO`

**Query Parameters**:
- `page` (default: 0)
- `size` (default: 20)
- `sort` (default: "id")

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "indirizzo": "Via Roma 1",
      "citta": "Milano",
      "superficie": 80.5,
      "tipo": "APPARTAMENTO",
      "piano": 3,
      "numCamere": 3
    }
  ],
  "totalElements": 50,
  ...
}
```

**Note**: La struttura varia in base al `tipo`:
- **APPARTAMENTO**: `piano`, `numCamere`
- **NEGOZIO**: `vetrine`, `magazzinoMq`
- **UFFICIO**: `postiLavoro`, `saleRiunioni`

---

### GET /api/immobili/{id}
Ottiene un immobile per ID.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_LOCATARIO`

**Response** (200 OK): Immobile completo

---

### POST /api/immobili
Crea un nuovo immobile.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body** (Appartamento):
```json
{
  "indirizzo": "Via Roma 1",
  "citta": "Milano",
  "superficie": 80.5,
  "tipo": "APPARTAMENTO",
  "piano": 3,
  "numCamere": 3
}
```

**Request Body** (Negozio):
```json
{
  "indirizzo": "Via Milano 10",
  "citta": "Roma",
  "superficie": 120.0,
  "tipo": "NEGOZIO",
  "vetrine": 2,
  "magazzinoMq": 30.0
}
```

**Request Body** (Ufficio):
```json
{
  "indirizzo": "Via Torino 5",
  "citta": "Torino",
  "superficie": 200.0,
  "tipo": "UFFICIO",
  "postiLavoro": 10,
  "saleRiunioni": 2
}
```

**Response** (201 Created): Immobile creato con `Location` header

---

### PUT /api/immobili/{id}
Aggiorna un immobile.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body**: Stesso formato di `POST /api/immobili`

**Response** (200 OK): Immobile aggiornato

---

### DELETE /api/immobili/{id}
Elimina un immobile.

**Autorizzazione**: `ROLE_ADMIN`

**Response** (204 No Content)

---

### GET /api/immobili/per-citta
Statistiche immobili affittati per città.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Response** (200 OK):
```json
{
  "Milano": 15,
  "Roma": 10,
  "Torino": 5
}
```

---

### GET /api/immobili/per-tipo
Conteggio immobili per tipo.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Response** (200 OK):
```json
{
  "APPARTAMENTO": 20,
  "NEGOZIO": 10,
  "UFFICIO": 5
}
```

---

## 📄 Contratti

### GET /api/contratti
Ottiene tutti i contratti (paginated).

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Query Parameters**: `page`, `size`, `sort`

**Response** (200 OK): Lista contratti paginata

---

### GET /api/contratti/{id}
Ottiene un contratto per ID.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_LOCATARIO`

**Controlli di Accesso**:
- **ADMIN e MANAGER**: Accesso completo a tutti i contratti
- **LOCATARIO**: Può accedere solo ai propri contratti
  - Se tenta di accedere a un contratto di un altro locatario → **403 Forbidden**

**Response** (200 OK):
```json
{
  "id": 1,
  "locatario": {
    "id": 1,
    "nome": "Mario",
    "cognome": "Rossi",
    ...
  },
  "immobile": {
    "id": 1,
    "indirizzo": "Via Roma 1",
    ...
  },
  "dataInizio": "2024-01-01",
  "durataAnni": 3,
  "canoneAnnuo": 12000.0,
  "frequenzaRata": "TRIMESTRALE",
  "rate": [...]
}
```

---

### GET /api/contratti/miei
Ottiene i contratti del locatario corrente.

**Autorizzazione**: `ROLE_LOCATARIO`

**Response** (200 OK): Lista contratti del locatario

---

### POST /api/contratti
Crea un nuovo contratto.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body**:
```json
{
  "locatarioId": 1,
  "immobileId": 1,
  "dataInizio": "2024-01-01",
  "durataAnni": 3,
  "canoneAnnuo": 12000.0,
  "frequenzaRata": "TRIMESTRALE"
}
```

**Response** (201 Created): Contratto creato con rate generate automaticamente

**Note**: Le rate vengono generate automaticamente in base a `frequenzaRata` e `durataAnni`.

---

### PUT /api/contratti/{id}
Aggiorna un contratto.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body**: Stesso formato di `POST /api/contratti`

---

### DELETE /api/contratti/{id}
Elimina un contratto.

**Autorizzazione**: `ROLE_ADMIN`

**Response** (204 No Content)

---

## 👥 Locatari

### GET /api/locatari
Ottiene tutti i locatari (paginated).

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Response** (200 OK): Lista locatari paginata

---

### GET /api/locatari/{id}
Ottiene un locatario per ID.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Response** (200 OK): Locatario completo

---

### GET /api/locatari/mio
Ottiene il locatario corrente (associato all'utente).

**Autorizzazione**: `ROLE_LOCATARIO`

**Response** (200 OK): Locatario dell'utente corrente

---

### POST /api/locatari
Crea un nuovo locatario.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body**:
```json
{
  "nome": "Mario",
  "cognome": "Rossi",
  "cf": "RSSMRA80A01H501X",
  "indirizzo": "Via Roma 1",
  "telefono": "1234567890",
  "userId": 1
}
```

**Validazioni**:
- `cf`: @NotBlank, UNIQUE
- `userId`: @NotNull, deve esistere

**Response** (201 Created): Locatario creato

---

### PUT /api/locatari/{id}
Aggiorna un locatario.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body**: Stesso formato di `POST /api/locatari`

---

### DELETE /api/locatari/{id}
Elimina un locatario.

**Autorizzazione**: `ROLE_ADMIN`

**Response** (204 No Content)

---

## 💰 Rate

### GET /api/rate
Ottiene tutte le rate.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Response** (200 OK): Lista rate

---

### GET /api/rate/{id}
Ottiene una rata per ID.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_LOCATARIO`

**Controlli di Accesso**:
- **ADMIN e MANAGER**: Accesso completo a tutte le rate
- **LOCATARIO**: Può accedere solo alle proprie rate
  - Se tenta di accedere a una rata di un altro locatario → **403 Forbidden**

**Response** (200 OK): Rata completa

**Errori**:
- **403 Forbidden**: LOCATARIO ha tentato di accedere a una rata non propria

---

### GET /api/rate/contratto/{contrattoId}
Ottiene le rate di un contratto.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_LOCATARIO`

**Controlli di Accesso**:
- **ADMIN e MANAGER**: Accesso completo a tutte le rate
- **LOCATARIO**: Può accedere solo alle rate dei propri contratti
  - Se tenta di accedere alle rate di un contratto di un altro locatario → **403 Forbidden**

**Response** (200 OK): Lista rate del contratto

**Errori**:
- **403 Forbidden**: LOCATARIO ha tentato di accedere alle rate di un contratto non proprio

---

### POST /api/rate
Crea una nuova rata.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body**:
```json
{
  "contrattoId": 1,
  "numeroRata": 1,
  "dataScadenza": "2024-03-31",
  "importo": 3000.0,
  "pagata": false
}
```

**Response** (201 Created): Rata creata

---

### PUT /api/rate/{id}
Aggiorna una rata.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body**: Stesso formato di `POST /api/rate`

---

### PATCH /api/rate/{id}/pagata
Segna una rata come pagata.

**Autorizzazione**: `ROLE_LOCATARIO`

**Response** (200 OK): Rata aggiornata con `pagata = true`

---

## 🔧 Manutenzioni

### GET /api/manutenzioni
Ottiene tutte le manutenzioni.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Response** (200 OK): Lista manutenzioni

---

### GET /api/manutenzioni/{id}
Ottiene una manutenzione per ID.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_LOCATARIO`

**Controlli di Accesso**:
- **ADMIN e MANAGER**: Accesso completo a tutte le manutenzioni
- **LOCATARIO**: Può accedere solo alle proprie manutenzioni
  - Se tenta di accedere a una manutenzione di un altro locatario → **403 Forbidden**

**Response** (200 OK): Manutenzione completa

**Errori**:
- **403 Forbidden**: LOCATARIO ha tentato di accedere a una manutenzione non propria

---

### POST /api/manutenzioni
Crea una nuova manutenzione.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body**:
```json
{
  "immobileId": 1,
  "locatarioId": 1,
  "dataMan": "2024-01-15",
  "importo": 500.0,
  "tipo": "ORDINARIA",
  "descrizione": "Riparazione caldaia"
}
```

**Response** (201 Created): Manutenzione creata

---

### POST /api/manutenzioni/mia
Crea una manutenzione per il locatario corrente.

**Autorizzazione**: `ROLE_LOCATARIO`

**Request Body**: Stesso formato, ma `locatarioId` viene preso dall'utente corrente

---

### PUT /api/manutenzioni/{id}
Aggiorna una manutenzione.

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Request Body**: Stesso formato di `POST /api/manutenzioni`

---

### DELETE /api/manutenzioni/{id}
Elimina una manutenzione.

**Autorizzazione**: `ROLE_ADMIN`

**Response** (204 No Content)

---

## 📤 Upload

### POST /api/upload/profile-image
Carica un'immagine profilo su Cloudinary.

**Autorizzazione**: `isAuthenticated()`

**Request**: `multipart/form-data`
- `file`: File immagine (max 10MB)

**Response** (200 OK):
```json
{
  "url": "https://res.cloudinary.com/.../profile_images/..."
}
```

**Note**:
- File viene caricato su Cloudinary
- URL restituito può essere usato per aggiornare `profileImage` dell'utente

---

## 📝 Convenzioni REST

### Status Codes
- **200 OK**: Richiesta riuscita
- **201 Created**: Risorsa creata
- **204 No Content**: Risorsa eliminata
- **400 Bad Request**: Errore validazione o business logic
- **401 Unauthorized**: Autenticazione richiesta
- **403 Forbidden**: Permessi insufficienti o accesso negato (es. LOCATARIO che tenta di accedere a risorsa non propria)
- **404 Not Found**: Risorsa non trovata
- **500 Internal Server Error**: Errore server

### Headers
- **Authorization**: `Bearer {token}` (per richieste autenticate)
- **Content-Type**: `application/json` (per body JSON)
- **Location**: URL risorsa creata (per 201 Created)

### Paginazione
Tutti gli endpoint che restituiscono liste supportano paginazione:
- `page`: Numero pagina (0-based)
- `size`: Dimensione pagina
- `sort`: Campo ordinamento (es: "id,desc")

### Error Response
Formato standard per errori:
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Messaggio errore",
  "path": "/api/immobili"
}
```

---

Per dettagli su GraphQL, consulta [GRAPHQL.md](./GRAPHQL.md).

