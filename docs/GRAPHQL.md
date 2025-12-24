# GraphQL API Documentation

Questo documento descrive l'implementazione **GraphQL** del progetto, inclusi schema, query, mutation e resolver.

## 📋 Panoramica

Il progetto espone un'**API GraphQL** completa che replica tutte le funzionalità REST, con i seguenti vantaggi:
- **Query flessibili**: Il client può richiedere solo i campi necessari
- **Single Endpoint**: Tutte le operazioni vanno a `/graphql`
- **Type Safety**: Schema tipizzato con validazione automatica
- **Field Resolver**: Risoluzione lazy delle relazioni

## 🔗 Endpoint GraphQL

### Base URL
```
POST http://localhost:8080/graphql
```

### GraphiQL UI
```
GET http://localhost:8080/graphiql
```

**Note**:
- GraphiQL UI è **pubblica** (non richiede autenticazione)
- Le **query GraphQL** richiedono autenticazione JWT
- Header richiesto: `Authorization: Bearer {token}`

## 📐 Schema GraphQL

Lo schema è definito in `src/main/resources/graphql/schema.graphqls`.

### Query Type

```graphql
type Query {
    # User Queries
    users: [User!]!
    user(id: ID!): User
    me: User
    
    # Immobile Queries
    immobili: [Immobile!]!
    immobile(id: ID!): Immobile
    
    # Contratto Queries
    contratti: [Contratto!]!
    contratto(id: ID!): Contratto
    
    # Locatario Queries
    locatari: [Locatario!]!
    locatario(id: ID!): Locatario
    
    # Rata Queries
    rate: [Rata!]!
    rata(id: ID!): Rata
    rateByContratto(contrattoId: ID!): [Rata!]!
    
    # Manutenzione Queries
    manutenzioni: [Manutenzione!]!
    manutenzione(id: ID!): Manutenzione
    
    # Role Queries
    roles: [Role!]!
}
```

### Mutation Type

```graphql
type Mutation {
    # User Mutations
    updateUser(id: ID!, input: UserUpdateInput!): User!
    updateMe(input: UserUpdateInput!): User!
    updateUserRoles(id: ID!, roles: [String!]!): User!
    deleteUser(id: ID!): Boolean!
    
    # Immobile Mutations
    createImmobile(input: ImmobileInput!): Immobile!
    updateImmobile(id: ID!, input: ImmobileInput!): Immobile!
    deleteImmobile(id: ID!): Boolean!
    
    # Contratto Mutations
    createContratto(input: ContrattoInput!): Contratto!
    updateContratto(id: ID!, input: ContrattoInput!): Contratto!
    deleteContratto(id: ID!): Boolean!
    
    # Locatario Mutations
    createLocatario(input: LocatarioInput!): Locatario!
    updateLocatario(id: ID!, input: LocatarioInput!): Locatario!
    deleteLocatario(id: ID!): Boolean!
    
    # Rata Mutations
    createRata(input: RataInput!): Rata!
    updateRata(id: ID!, input: RataInput!): Rata!
    deleteRata(id: ID!): Boolean!
    markRataAsPagata(id: ID!): Rata!
    
    # Manutenzione Mutations
    createManutenzione(input: ManutenzioneInput!): Manutenzione!
    updateManutenzione(id: ID!, input: ManutenzioneInput!): Manutenzione!
    deleteManutenzione(id: ID!): Boolean!
}
```

## 🔍 Query Examples

### Get Current User

**Query**:
```graphql
query {
  me {
    id
    email
    nome
    cognome
    profileImage
    registrationDate
    enabled
    roles {
      id
      name
    }
  }
}
```

**Autorizzazione**: `isAuthenticated()`

**Response**:
```json
{
  "data": {
    "me": {
      "id": "1",
      "email": "user@example.com",
      "nome": "Mario",
      "cognome": "Rossi",
      "profileImage": "https://cloudinary.com/...",
      "registrationDate": "2024-01-01",
      "enabled": true,
      "roles": [
        {"id": "1", "name": "ROLE_ADMIN"}
      ]
    }
  }
}
```

---

### Get All Users (Admin Only)

**Query**:
```graphql
query {
  users {
    id
    email
    nome
    cognome
    roles {
      name
    }
  }
}
```

**Autorizzazione**: `ROLE_ADMIN`

---

### Get Immobile with Relations

**Query**:
```graphql
query {
  immobile(id: "1") {
    id
    indirizzo
    citta
    superficie
    tipo
    contratti {
      id
      dataInizio
      canoneAnnuo
      locatario {
        nome
        cognome
      }
    }
    manutenzioni {
      id
      dataMan
      importo
      tipo
    }
  }
}
```

**Autorizzazione**: `isAuthenticated()`

**Note**: Le relazioni `contratti` e `manutenzioni` vengono risolte tramite `FieldResolver` (lazy loading).

---

### Get Rate by Contratto

**Query**:
```graphql
query {
  rateByContratto(contrattoId: "1") {
    id
    numeroRata
    dataScadenza
    importo
    pagata
  }
}
```

**Autorizzazione**: `isAuthenticated()`

---

## ✏️ Mutation Examples

### Update Current User

**Mutation**:
```graphql
mutation {
  updateMe(input: {
    nome: "Mario"
    cognome: "Rossi"
    profileImage: "https://cloudinary.com/..."
  }) {
    id
    nome
    cognome
    profileImage
  }
}
```

**Autorizzazione**: `isAuthenticated()`

---

### Create Immobile

**Mutation** (Appartamento):
```graphql
mutation {
  createImmobile(input: {
    indirizzo: "Via Roma 1"
    citta: "Milano"
    superficie: 80.5
    tipo: APPARTAMENTO
    piano: 3
    numCamere: 3
  }) {
    id
    indirizzo
    tipo
    piano
    numCamere
  }
}
```

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Mutation** (Negozio):
```graphql
mutation {
  createImmobile(input: {
    indirizzo: "Via Milano 10"
    citta: "Roma"
    superficie: 120.0
    tipo: NEGOZIO
    vetrine: 2
    magazzinoMq: 30.0
  }) {
    id
    tipo
    vetrine
    magazzinoMq
  }
}
```

---

### Create Contratto

**Mutation**:
```graphql
mutation {
  createContratto(input: {
    locatarioId: "1"
    immobileId: "1"
    dataInizio: "2024-01-01"
    durataAnni: 3
    canoneAnnuo: 12000.0
    frequenzaRata: TRIMESTRALE
  }) {
    id
    dataInizio
    durataAnni
    canoneAnnuo
    frequenzaRata
    rate {
      id
      numeroRata
      dataScadenza
      importo
    }
  }
}
```

**Autorizzazione**: `ROLE_ADMIN`, `ROLE_MANAGER`

**Note**: Le rate vengono generate automaticamente in base a `frequenzaRata` e `durataAnni`.

---

### Mark Rata as Pagata

**Mutation**:
```graphql
mutation {
  markRataAsPagata(id: "1") {
    id
    pagata
    dataScadenza
    importo
  }
}
```

**Autorizzazione**: `ROLE_LOCATARIO`

---

### Update User Roles

**Mutation**:
```graphql
mutation {
  updateUserRoles(id: "1", roles: ["ROLE_MANAGER", "ROLE_LOCATARIO"]) {
    id
    email
    roles {
      id
      name
    }
  }
}
```

**Autorizzazione**: `ROLE_ADMIN`

---

## 🏗️ Resolver Implementation

### QueryResolver

Classe `QueryResolver.java` che risolve tutte le query GraphQL.

**Pattern**:
```java
@QueryMapping
@PreAuthorize("hasRole('ADMIN')")
public List<User> users() {
    return userService.getAllUsers();
}
```

**Caratteristiche**:
- Ogni metodo è annotato con `@QueryMapping`
- Autorizzazione tramite `@PreAuthorize`
- Utilizza i service per business logic

### MutationResolver

Classe `MutationResolver.java` che risolve tutte le mutation GraphQL.

**Pattern**:
```java
@MutationMapping
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public Immobile createImmobile(@Argument ImmobileInput input) {
    ImmobileRequestDTO dto = ImmobileRequestDTO.builder()
            .indirizzo(input.getIndirizzo())
            .citta(input.getCitta())
            // ...
            .build();
    return immobileService.createImmobile(dto);
}
```

**Caratteristiche**:
- Ogni metodo è annotato con `@MutationMapping`
- Input types vengono convertiti in DTO
- Utilizza i service per business logic

### FieldResolver

Classe `FieldResolver.java` che risolve campi lazy (relazioni).

**Esempio**:
```java
@SchemaMapping(typeName = "Immobile", field = "contratti")
public List<Contratto> contratti(Immobile immobile) {
    return contrattoService.getContrattiByImmobileId(immobile.getId());
}
```

**Caratteristiche**:
- Risolve relazioni `@OneToMany` e `@ManyToOne` lazy
- Evita N+1 problem caricando relazioni solo quando richieste
- Utilizza `@SchemaMapping` per mappare campi specifici

## 📦 Input Types

### UserUpdateInput
```graphql
input UserUpdateInput {
    nome: String
    cognome: String
    profileImage: String
}
```

### ImmobileInput
```graphql
input ImmobileInput {
    indirizzo: String!
    citta: String!
    superficie: Float!
    tipo: TipoImmobile!
    piano: Int              # Solo per APPARTAMENTO
    numCamere: Int           # Solo per APPARTAMENTO
    postiLavoro: Int         # Solo per UFFICIO
    saleRiunioni: Int        # Solo per UFFICIO
    vetrine: Int             # Solo per NEGOZIO
    magazzinoMq: Float       # Solo per NEGOZIO
}
```

### ContrattoInput
```graphql
input ContrattoInput {
    locatarioId: ID!
    immobileId: ID!
    dataInizio: String!
    durataAnni: Int!
    canoneAnnuo: Float!
    frequenzaRata: FrequenzaRata!
}
```

### LocatarioInput
```graphql
input LocatarioInput {
    nome: String!
    cognome: String!
    cf: String!
    indirizzo: String!
    telefono: String!
    userId: ID!
}
```

### RataInput
```graphql
input RataInput {
    contrattoId: ID!
    numeroRata: Int!
    dataScadenza: String!
    importo: Float!
    pagata: Boolean
}
```

### ManutenzioneInput
```graphql
input ManutenzioneInput {
    immobileId: ID!
    locatarioId: ID!
    dataMan: String!
    importo: Float!
    tipo: String!
    descrizione: String
}
```

## 🔐 Autorizzazione GraphQL

L'autorizzazione funziona esattamente come per REST:

```java
@QueryMapping
@PreAuthorize("hasRole('ADMIN')")
public List<User> users() { ... }

@MutationMapping
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public Immobile createImmobile(@Argument ImmobileInput input) { ... }
```

**Note**:
- `@PreAuthorize` funziona anche su resolver GraphQL
- L'autenticazione viene gestita da `JwtAuthenticationFilter` prima che la richiesta arrivi a GraphQL
- Se non autenticato, viene restituito errore 401

## 📝 Utilizzo in Postman

### Request Setup

**Method**: `POST`

**URL**: `http://localhost:8080/graphql`

**Headers**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
  "query": "query { me { id email nome cognome } }"
}
```

### Con Variables

**Body**:
```json
{
  "query": "query($id: ID!) { immobile(id: $id) { id indirizzo citta } }",
  "variables": {
    "id": "1"
  }
}
```

### Mutation Example

**Body**:
```json
{
  "query": "mutation($input: ImmobileInput!) { createImmobile(input: $input) { id indirizzo } }",
  "variables": {
    "input": {
      "indirizzo": "Via Roma 1",
      "citta": "Milano",
      "superficie": 80.5,
      "tipo": "APPARTAMENTO",
      "piano": 3,
      "numCamere": 3
    }
  }
}
```

## 🚨 Error Handling

### Error Response Format

```json
{
  "errors": [
    {
      "message": "Access Denied",
      "locations": [{"line": 2, "column": 3}],
      "path": ["users"],
      "extensions": {
        "classification": "DataFetchingException"
      }
    }
  ],
  "data": null
}
```

### Errori Comuni

#### **401 Unauthorized**
- **Causa**: Token mancante o non valido
- **Soluzione**: Effettuare login REST per ottenere token

#### **403 Forbidden**
- **Causa**: Ruolo insufficiente
- **Soluzione**: Verificare ruolo utente

#### **Validation Error**
- **Causa**: Input non valido
- **Esempio**: Campo obbligatorio mancante, tipo errato

## 🎯 Vantaggi GraphQL vs REST

### GraphQL
✅ **Query Flessibili**: Richiedi solo i campi necessari  
✅ **Single Endpoint**: Una sola URL per tutte le operazioni  
✅ **Type Safety**: Schema tipizzato con validazione  
✅ **Field Resolver**: Risoluzione lazy delle relazioni  
✅ **Introspection**: Schema esplorabile con GraphiQL

### REST
✅ **Semplicità**: Endpoint dedicati per ogni operazione  
✅ **Caching**: Più facile implementare caching HTTP  
✅ **Standard**: Più diffuso e supportato  
✅ **Tooling**: Maggiore supporto da tool esterni

## 📚 Best Practices

✅ **Schema-First**: Schema definito in `.graphqls` file  
✅ **Input Types**: Utilizza input types per mutation  
✅ **Field Resolver**: Risolvi relazioni lazy con FieldResolver  
✅ **Authorization**: Usa `@PreAuthorize` su ogni resolver  
✅ **Error Handling**: Gestisci errori in modo consistente  
✅ **Validation**: Valida input tramite DTO conversion

---

Per dettagli su REST API, consulta [REST_API.md](./REST_API.md).

