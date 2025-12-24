# Autenticazione e Autorizzazione

Questo documento descrive il sistema di **autenticazione JWT** e **autorizzazione basata su ruoli** implementato nel progetto.

## 🔐 Panoramica

Il sistema utilizza:
- **JWT (JSON Web Token)** per autenticazione stateless
- **Spring Security** per gestione sicurezza
- **Role-Based Access Control (RBAC)** con 3 ruoli principali
- **BCrypt** per hashing delle password

## 🔑 Autenticazione JWT

### Flusso di Autenticazione

```
1. Client invia credenziali → POST /api/auth/login
   ↓
2. AuthService valida credenziali
   ↓
3. JwtTokenProvider genera token JWT
   ↓
4. Token restituito al client
   ↓
5. Client include token in header Authorization: Bearer {token}
   ↓
6. JwtAuthenticationFilter estrae e valida token
   ↓
7. Spring Security crea Authentication object
   ↓
8. Request processata con utente autenticato
```

### Componenti JWT

#### **JwtTokenProvider**
Classe che gestisce generazione e validazione token.

**Metodi principali**:
- `generateToken(String username)`: Genera token JWT
- `extractUsername(String token)`: Estrae username dal token
- `validateToken(String token, UserDetails)`: Valida token
- `isTokenExpired(String token)`: Verifica scadenza

**Configurazione**:
- **Secret Key**: Leggibile da `jwt.secret` in `env.properties`
- **Expiration**: 86400000 ms (24 ore) - configurabile in `application.properties`

#### **JwtAuthenticationFilter**
Filter che intercetta richieste e estrae token JWT dall'header `Authorization`.

**Funzionamento**:
1. Estrae token da header `Authorization: Bearer {token}`
2. Valida token con `JwtTokenProvider`
3. Carica `UserDetails` tramite `CustomUserDetailsService`
4. Crea `UsernamePasswordAuthenticationToken`
5. Imposta `SecurityContextHolder` con authentication

**Posizione nella Filter Chain**:
- Eseguito **prima** di `UsernamePasswordAuthenticationFilter`
- Configurato in `SecurityConfig.java`

### Endpoint di Autenticazione

#### **POST /api/auth/register**
Registra un nuovo utente.

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

**Note**:
- Password viene hashata con BCrypt
- Email deve essere univoca
- Utente creato con `enabled = true`
- Ruolo di default: nessuno (deve essere assegnato da ADMIN)

#### **POST /api/auth/login**
Autentica un utente esistente.

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
- **401 Unauthorized**: Credenziali non valide
- **401 Unauthorized**: Utente disabilitato (`enabled = false`)

### Utilizzo Token nelle Richieste

Dopo il login, includere il token in tutte le richieste protette:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Esempio cURL**:
```bash
curl -H "Authorization: Bearer {token}" \
     http://localhost:8080/api/users/me
```

**Esempio Postman**:
- Header: `Authorization`
- Value: `Bearer {token}`

## 👥 Sistema di Ruoli

### Ruoli Disponibili

Il sistema prevede **3 ruoli principali**:

#### **ROLE_ADMIN**
- **Descrizione**: Amministratore con accesso completo
- **Permessi**:
  - Gestione completa utenti (CRUD)
  - Gestione completa immobili (CRUD + DELETE)
  - Gestione completa contratti (CRUD + DELETE)
  - Gestione completa locatari (CRUD + DELETE)
  - Gestione completa rate (CRUD + DELETE)
  - Gestione completa manutenzioni (CRUD + DELETE)
  - Assegnazione ruoli agli utenti
  - Statistiche e report

#### **ROLE_MANAGER**
- **Descrizione**: Manager con permessi di gestione operativa
- **Permessi**:
  - Visualizzazione immobili
  - Creazione e modifica immobili (NO DELETE)
  - Gestione contratti (CRUD, NO DELETE)
  - Gestione locatari (CRUD, NO DELETE)
  - Gestione rate (CRUD, NO DELETE)
  - Gestione manutenzioni (CRUD, NO DELETE)
  - Visualizzazione statistiche

#### **ROLE_LOCATARIO**
- **Descrizione**: Locatario con accesso limitato ai propri dati
- **Permessi**:
  - Visualizzazione propri dati utente
  - Visualizzazione propri contratti (solo i propri)
  - Visualizzazione proprie rate (solo le proprie)
  - Visualizzazione proprie manutenzioni (solo le proprie)
  - Creazione manutenzioni per propri immobili
  - Pagamento rate (mark as pagata)
- **Controlli di Accesso**: 
  - Gli endpoint "by id" verificano che la risorsa appartenga al locatario corrente
  - Tentativi di accesso a risorse di altri locatari restituiscono **403 Forbidden**

### Assegnazione Ruoli

I ruoli vengono assegnati tramite:
- **REST API**: `PUT /api/users/{id}/roles` (solo ADMIN)
- **GraphQL**: `mutation { updateUserRoles(id: ID!, roles: [String!]!) }` (solo ADMIN)

**Esempio**:
```json
PUT /api/users/1/roles
{
  "roles": ["ROLE_MANAGER", "ROLE_LOCATARIO"]
}
```

## 🛡️ Autorizzazione

### Method-Level Security

L'autorizzazione viene gestita tramite **`@PreAuthorize`** su controller e resolver.

#### **Espressioni Comuni**

```java
@PreAuthorize("hasRole('ADMIN')")                    // Solo ADMIN
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")      // ADMIN o MANAGER
@PreAuthorize("hasRole('LOCATARIO')")                // Solo LOCATARIO
@PreAuthorize("isAuthenticated()")                    // Qualsiasi utente autenticato
@PreAuthorize("hasAuthority('ROLE_ADMIN')")          // Alternativa a hasRole
```

#### **Controlli di Accesso per LOCATARIO**

Per gli endpoint "by id" accessibili ai LOCATARIO, viene implementato un **controllo aggiuntivo** che verifica l'appartenenza della risorsa al locatario corrente. Questo previene il **Broken Access Control**, impedendo ai locatari di accedere a risorse di altri locatari conoscendo l'ID.

**Endpoint protetti con controllo di appartenenza**:
- `GET /api/contratti/{id}` - Verifica che il contratto appartenga al locatario
- `GET /api/rate/{id}` - Verifica che la rata appartenga al locatario
- `GET /api/rate/contratto/{contrattoId}` - Verifica che il contratto appartenga al locatario
- `GET /api/manutenzioni/{id}` - Verifica che la manutenzione appartenga al locatario

**Implementazione**:
```java
@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'LOCATARIO')")
public ResponseEntity<Contratto> getContrattoById(
        @PathVariable Long id,
        Authentication authentication) {
    Contratto contratto = contrattoService.getContrattoById(id);
    
    // Verifica che se è LOCATARIO, il contratto appartenga al locatario corrente
    if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_LOCATARIO"))) {
        String email = authentication.getName();
        List<Contratto> myContratti = contrattoService.getContrattiByLocatarioEmail(email);
        boolean isMyContratto = myContratti.stream()
                .anyMatch(c -> c.getId().equals(id));
        if (!isMyContratto) {
            throw new AccessDeniedException("Non hai accesso a questo contratto");
        }
    }
    
    return ResponseEntity.ok(contratto);
}
```

**Comportamento**:
- **ADMIN e MANAGER**: Accesso completo a tutte le risorse (nessun controllo aggiuntivo)
- **LOCATARIO**: Può accedere solo alle proprie risorse
  - Se tenta di accedere a risorse di altri locatari → **403 Forbidden**
  - Il controllo viene effettuato dopo il recupero della risorsa dal database

#### **Esempi di Utilizzo**

**Controller REST**:
```java
@GetMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<User>> getAllUsers() {
    // Solo ADMIN può vedere tutti gli utenti
}

@PostMapping("/api/immobili")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public ResponseEntity<Immobile> createImmobile(...) {
    // ADMIN e MANAGER possono creare immobili
}
```

**GraphQL Resolver**:
```java
@QueryMapping
@PreAuthorize("hasRole('ADMIN')")
public List<User> users() {
    // Solo ADMIN può eseguire questa query
}

@MutationMapping
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public Immobile createImmobile(@Argument ImmobileInput input) {
    // ADMIN e MANAGER possono creare immobili
}
```

### Configurazione Security

#### **SecurityConfig.java**

Configurazione principale in `SecurityConfig.java`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()      // Pubblico
            .requestMatchers("/swagger-ui/**").permitAll()    // Pubblico
            .requestMatchers("/graphiql").permitAll()         // Pubblico (solo UI)
            .requestMatchers("/graphql").authenticated()      // Richiede auth
            .anyRequest().authenticated()                      // Tutto il resto richiede auth
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

**Note**:
- **Stateless**: Nessuna sessione server-side (JWT)
- **CSRF Disabled**: Non necessario per API stateless
- **GraphQL**: Endpoint `/graphql` richiede autenticazione, ma GraphiQL UI è pubblica

### Endpoint Pubblici vs Protetti

#### **Endpoint Pubblici** (senza autenticazione)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /swagger-ui/**`
- `GET /api-docs/**`
- `GET /graphiql` (solo UI, le query richiedono auth)

#### **Endpoint Protetti** (richiedono autenticazione)
- Tutti gli altri endpoint REST (`/api/**`)
- Endpoint GraphQL (`POST /graphql`)

## 🔒 Password Security

### Hashing con BCrypt

Le password vengono hashate con **BCrypt** prima di essere salvate nel database.

**Configurazione**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Caratteristiche BCrypt**:
- **Salt automatico**: Ogni password ha un salt univoco
- **Cost factor**: 10 (configurabile, default Spring Security)
- **One-way hash**: Impossibile decriptare

**Esempio**:
```
Password originale: "password123"
Hash BCrypt: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

### Validazione Password

La validazione password avviene in `AuthService`:

```java
if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
    throw new BadCredentialsException("Email o password non validi");
}
```

## 🚨 Gestione Errori di Autenticazione

### Errori Comuni

#### **401 Unauthorized**
- **Causa**: Token mancante, scaduto o non valido
- **Messaggio**: "Autenticazione richiesta. Effettua il login per accedere."
- **Soluzione**: Effettuare login per ottenere nuovo token

#### **403 Forbidden**
- **Causa**: Utente autenticato ma senza permessi sufficienti
- **Messaggio**: 
  - Generico: "Non hai i permessi per accedere a questa risorsa."
  - LOCATARIO su risorsa non propria: "Non hai accesso a questo contratto/rata/manutenzione"
- **Soluzione**: 
  - Verificare ruolo utente o contattare amministratore
  - Per LOCATARIO: Verificare che la risorsa richiesta appartenga al proprio account

#### **401 Bad Credentials**
- **Causa**: Email o password non corretti
- **Messaggio**: "Email o password non validi"
- **Soluzione**: Verificare credenziali

### Gestione Centralizzata

Gli errori di autenticazione vengono gestiti da:
- **SecurityConfig**: `authenticationEntryPoint` e `accessDeniedHandler`
- **GlobalExceptionHandler**: Gestione `AuthenticationException` e `AccessDeniedException`

## 📝 Best Practices Implementate

✅ **Stateless Authentication**: JWT senza sessioni server-side  
✅ **Password Hashing**: BCrypt con salt automatico  
✅ **Token Expiration**: Token scadono dopo 24 ore  
✅ **Method-Level Security**: `@PreAuthorize` su ogni endpoint  
✅ **Role-Based Access**: 3 ruoli con permessi chiari  
✅ **Access Control**: Controlli di appartenenza per LOCATARIO negli endpoint "by id"  
✅ **Broken Access Control Prevention**: Verifica che i LOCATARIO accedano solo alle proprie risorse  
✅ **Error Handling**: Messaggi di errore chiari e consistenti  
✅ **CORS Configuration**: Configurato per frontend specifici  
✅ **Security Headers**: Configurati tramite Spring Security

## 🔄 Refresh Token (Future Enhancement)

Attualmente il sistema **non implementa refresh token**. Per rinnovare il token:
1. Effettuare nuovo login
2. Ottenere nuovo token JWT

**Possibile miglioramento futuro**:
- Implementare refresh token con scadenza più lunga
- Endpoint `/api/auth/refresh` per rinnovare token senza login

---

Per dettagli su endpoint REST, consulta [REST_API.md](./REST_API.md).

