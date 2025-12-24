# Integrazioni con API Esterne

Questo documento descrive le **integrazioni con API esterne** implementate nel progetto.

## 📋 Panoramica

Il progetto integra **2 API esterne**:
1. **Cloudinary**: Upload e gestione immagini profilo
2. **Mailgun**: Invio email di notifica

## ☁️ Cloudinary

### Descrizione

**Cloudinary** è un servizio cloud per gestione media (immagini, video). Nel progetto viene utilizzato per:
- Upload immagini profilo utente
- Storage sicuro e scalabile
- CDN per distribuzione globale
- Trasformazioni automatiche (resize, crop, etc.)

### Implementazione

#### **CloudinaryService.java**

**Classe**: `com.epicode.Progetto_Backend.service.CloudinaryService`

**Metodo principale**:
```java
public String uploadImage(MultipartFile file) throws IOException {
    Map<String, Object> uploadResult = cloudinary.uploader().upload(
        file.getBytes(),
        ObjectUtils.asMap(
            "folder", "profile_images",
            "resource_type", "auto"
        )
    );
    return (String) uploadResult.get("secure_url");
}
```

**Caratteristiche**:
- Riceve file `MultipartFile` dal controller
- Carica su Cloudinary nella cartella `profile_images`
- Restituisce URL pubblico (`secure_url`) dell'immagine
- `resource_type: auto` rileva automaticamente tipo file (JPG, PNG, GIF, WebP)

#### **CloudinaryProperties.java**

**Classe**: `com.epicode.Progetto_Backend.config.CloudinaryProperties`

**Configurazione**:
```java
@ConfigurationProperties(prefix = "cloudinary")
@Data
public class CloudinaryProperties {
    private String cloudName;
    private String apiKey;
    private String apiSecret;
}
```

**Properties** (in `env.properties`):
```properties
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
```

### Endpoint REST

#### **POST /api/upload/profile-image**

**Autorizzazione**: `isAuthenticated()`

**Request**:
- **Content-Type**: `multipart/form-data`
- **Body**: `file` (immagine, max 10MB)

**Response** (200 OK):
```json
{
  "url": "https://res.cloudinary.com/{cloud_name}/image/upload/v1234567890/profile_images/{file_id}.jpg"
}
```

**Esempio cURL**:
```bash
curl -X POST \
  -H "Authorization: Bearer {token}" \
  -F "file=@profile.jpg" \
  http://localhost:8080/api/upload/profile-image
```

### Utilizzo nel Flusso

1. **Upload Immagine**:
   ```
   Client → POST /api/upload/profile-image → CloudinaryService → Cloudinary API
   ```

2. **Aggiornamento Profilo**:
   ```
   Client → PUT /api/users/me → UserService → Salva URL in database
   ```

**Esempio completo**:
```javascript
// 1. Upload immagine
const formData = new FormData();
formData.append('file', fileInput.files[0]);

const uploadResponse = await fetch('/api/upload/profile-image', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` },
  body: formData
});

const { url } = await uploadResponse.json();

// 2. Aggiorna profilo con URL
await fetch('/api/users/me', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ profileImage: url })
});
```

### Configurazione File Upload

**application.properties**:
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Vantaggi Cloudinary

✅ **CDN Globale**: Immagini servite da CDN per performance ottimali  
✅ **Trasformazioni**: Resize, crop, watermark automatici  
✅ **Ottimizzazione**: Compressione automatica immagini  
✅ **Sicurezza**: URL firmati per accesso controllato  
✅ **Scalabilità**: Storage illimitato e scalabile

### Limitazioni e Considerazioni

⚠️ **Quota Gratuita**: 25GB storage, 25GB bandwidth/mese  
⚠️ **Costi**: Oltre la quota gratuita, costi per GB  
⚠️ **Dipendenza Esterna**: L'applicazione dipende da Cloudinary per funzionare  
⚠️ **Fallback**: In caso di errore Cloudinary, l'upload fallisce (gestire errore nel frontend)

---

## 📧 Mailgun

### Descrizione

**Mailgun** è un servizio per invio email transazionali. Nel progetto viene utilizzato per:
- Email di benvenuto per nuovi utenti
- Notifiche di creazione contratti
- Conferme e promemoria pagamenti rate
- Conferme richieste manutenzione
- Solleciti per pagamenti scaduti

### Implementazione

#### **MailgunService.java**

**Classe**: `com.epicode.Progetto_Backend.service.MailgunService`

**Caratteristiche principali**:
- 📧 Invio email in formato **testo** e **HTML**
- ⚡ Invio **asincrono** (`@Async`) per non bloccare le operazioni
- 📝 **Template HTML** professionali con branding
- 🔔 Notifiche per tutti gli eventi chiave del sistema
- ✅ Logging dettagliato di successi ed errori

**Metodi disponibili**:

| Metodo | Descrizione | Trigger |
|--------|-------------|---------|
| `sendEmail()` | Email testo semplice | Base |
| `sendHtmlEmail()` | Email HTML formattata | Base |
| `sendWelcomeEmail()` | Benvenuto nuovo utente | Registrazione |
| `sendContractNotification()` | Nuovo contratto creato | Creazione contratto |
| `sendContractExpirationNotification()` | Contratto in scadenza | Batch/Scheduler |
| `sendPaymentReminderEmail()` | Promemoria rata in scadenza | Batch/Scheduler |
| `sendPaymentConfirmationEmail()` | Conferma pagamento rata | Pagamento rata |
| `sendOverduePaymentNotification()` | Sollecito rata scaduta | Batch/Scheduler |
| `sendMaintenanceRequestConfirmation()` | Conferma richiesta manutenzione | Creazione manutenzione |
| `sendMaintenanceCompletedNotification()` | Manutenzione completata | Aggiornamento manutenzione |

**Metodo base per invio email**:
```java
public void sendEmail(String to, String subject, String text) {
    logger.info("Invio email a: {} - Oggetto: {}", to, subject);
    
    HttpResponse<String> response = Unirest.post(getApiUrl())
            .basicAuth("api", mailgunProperties.getApiKey())
            .field("from", mailgunProperties.getFromEmail())
            .field("to", to)
            .field("subject", subject)
            .field("text", text)
            .asString();

    handleResponse(response, to, subject);
}
```

**Invio email HTML (asincrono)**:
```java
@Async
public void sendHtmlEmailAsync(String to, String subject, String htmlContent) {
    sendHtmlEmail(to, subject, htmlContent);
}
```

#### **MailgunProperties.java**

**Classe**: `com.epicode.Progetto_Backend.config.MailgunProperties`

**Configurazione**:
```java
@ConfigurationProperties(prefix = "mailgun")
@Data
public class MailgunProperties {
    private String apiKey;
    private String domain;
    private String fromEmail;
}
```

**Properties** (in `env.properties`):
```properties
mailgun.api-key=your_mailgun_api_key
mailgun.domain=your_mailgun_domain
mailgun.from-email=noreply@yourdomain.com
```

### Template Email HTML

Tutte le email utilizzano un **template HTML professionale** con:
- 🎨 Header con gradiente e logo
- 📦 Box informativi colorati (successo, warning, errore)
- 📱 Design responsive
- 🔒 Footer con disclaimer

**Esempio Template**:
```html
<div style="background: linear-gradient(135deg, #667eea, #764ba2); padding: 30px;">
    <h1 style="color: white;">🏠 Cooperativa Immobiliare</h1>
</div>
<div style="padding: 30px; background: #fff;">
    <h2>Titolo Email</h2>
    <p>Contenuto personalizzato...</p>
    <div style="background: #d4edda; padding: 15px; border-radius: 8px;">
        <!-- Box informativo -->
    </div>
</div>
```

### Tipi di Notifiche

#### **1. Email di Benvenuto** 🎉

**Trigger**: Registrazione nuovo utente in `AuthService`

```java
// AuthService.register()
mailgunService.sendWelcomeEmail(user.getEmail(), fullName);
```

**Contenuto**:
- Messaggio di benvenuto personalizzato
- Lista funzionalità disponibili
- Link implicito all'area riservata

#### **2. Notifica Nuovo Contratto** 📝

**Trigger**: Creazione contratto in `ContrattoService`

```java
// ContrattoService.createContratto()
mailgunService.sendContractNotification(
    locatarioEmail, 
    locatarioName, 
    immobileIndirizzo
);
```

**Contenuto**:
- Conferma creazione contratto
- Indirizzo immobile
- Invito a consultare area riservata

#### **3. Conferma Pagamento** ✅

**Trigger**: Aggiornamento rata pagata in `RataService`

```java
// RataService.updateRataPagata()
if (pagata == 'S' && vecchioStato == 'N') {
    mailgunService.sendPaymentConfirmationEmail(...);
}
```

**Contenuto**:
- Numero rata pagata
- Importo ricevuto
- Riferimento immobile

#### **4. Promemoria Pagamento** 🔔

**Trigger**: Scheduler/Batch job (da implementare)

```java
mailgunService.sendPaymentReminderEmail(
    email, nome, numeroRata, importo, dataScadenza, indirizzo
);
```

**Contenuto**:
- Dettagli rata in scadenza
- Importo e data scadenza
- Sollecito pagamento

#### **5. Sollecito Rata Scaduta** ❌

**Trigger**: Scheduler/Batch job (da implementare)

```java
mailgunService.sendOverduePaymentNotification(
    email, nome, numeroRata, importo, dataScadenza, indirizzo
);
```

**Contenuto**:
- Avviso pagamento scaduto
- Box rosso con dettagli
- Richiesta urgente pagamento

#### **6. Conferma Manutenzione** 🔧

**Trigger**: Creazione manutenzione in `ManutenzioneService`

```java
// ManutenzioneService.createManutenzione()
mailgunService.sendMaintenanceRequestConfirmation(
    email, nome, indirizzo, tipo, descrizione, data
);
```

**Contenuto**:
- Conferma registrazione richiesta
- Dettagli intervento
- Promessa di contatto

### Integrazione nei Service

#### **AuthService** (Registrazione)
```java
@Autowired
private MailgunService mailgunService;

@Transactional
public AuthResponseDTO register(RegisterRequestDTO request) {
    // ... creazione utente ...
    
    // Invia email di benvenuto (asincrono)
    String fullName = user.getNome() + " " + user.getCognome();
    mailgunService.sendWelcomeEmail(user.getEmail(), fullName);
    
    // ... resto del metodo ...
}
```

#### **ContrattoService** (Nuovo Contratto)
```java
@RequiredArgsConstructor
public class ContrattoService {
    private final MailgunService mailgunService;
    
    @Transactional
    public Contratto createContratto(ContrattoRequestDTO request) {
        // ... creazione contratto ...
        
        // Invia notifica email al locatario (asincrono)
        mailgunService.sendContractNotification(
            locatarioEmail, locatarioName, immobileIndirizzo
        );
        
        return savedContratto;
    }
}
```

#### **RataService** (Pagamento Rata)
```java
@Transactional
public Rata updateRataPagata(Long id, Character pagata) {
    Rata rata = getRataById(id);
    Character vecchioStato = rata.getPagata();
    rata.setPagata(pagata);
    Rata updated = rataRepository.save(rata);
    
    // Invia conferma pagamento se appena pagata
    if (pagata == 'S' && vecchioStato == 'N') {
        sendPaymentConfirmationForRata(updated);
    }
    
    return updated;
}
```

#### **ManutenzioneService** (Richiesta Manutenzione)
```java
@Transactional
public Manutenzione createManutenzione(ManutenzioneRequestDTO request) {
    // ... creazione manutenzione ...
    
    // Invia email conferma (asincrono)
    mailgunService.sendMaintenanceRequestConfirmation(
        email, nome, indirizzo, tipo, descrizione, data
    );
    
    return saved;
}
```

### Configurazione Mailgun

#### **Setup Account**

1. Registrati su [mailgun.com](https://mailgun.com)
2. Verifica dominio (o usa sandbox per test)
3. Ottieni:
   - **API Key**: Chiave API dal dashboard
   - **Domain**: Dominio verificato (es: `mg.yourdomain.com`)
   - **From Email**: Email mittente (es: `noreply@yourdomain.com`)

#### **Sandbox (Sviluppo)**

Per sviluppo, puoi usare il sandbox Mailgun:
- **Domain**: `sandbox{xxx}.mailgun.org`
- **Limite**: 300 email/giorno
- **Destinatari**: Solo email verificate nel dashboard

**Configurazione sandbox**:
```properties
mailgun.domain=sandbox1234567890.mailgun.org
mailgun.from-email=postmaster@sandbox1234567890.mailgun.org
```

### Vantaggi Mailgun

✅ **Affidabilità**: Delivery rate > 99%  
✅ **API Semplice**: REST API facile da integrare  
✅ **Analytics**: Tracking aperture, click, bounce  
✅ **Sandbox**: Ambiente di test gratuito  
✅ **Scalabilità**: Gestisce milioni di email

### Limitazioni e Considerazioni

⚠️ **Quota Gratuita**: 5.000 email/mese per 3 mesi, poi a pagamento  
⚠️ **Sandbox Limiti**: Solo 300 email/giorno, solo destinatari autorizzati  
⚠️ **Dipendenza Esterna**: L'applicazione dipende da Mailgun per funzionare  
⚠️ **Error Handling**: Gestire fallimenti email senza bloccare operazioni principali  
⚠️ **Asincrono**: Considerare invio asincrono per non rallentare risposte API

### Best Practices Implementate

✅ **Error Handling**: Try-catch per non bloccare operazioni principali  
✅ **Logging**: Log errori email senza esporre dettagli al client  
✅ **Non Bloccante**: Invio email non blocca risposta API  
✅ **Template Separati**: Metodi dedicati per ogni tipo di email

---

## 🔄 Flusso Completo Integrazioni

### Scenario: Nuovo Utente con Immagine Profilo

```
1. Client → POST /api/auth/register
   ↓
2. AuthService crea utente
   ↓
3. AuthService invia email benvenuto (Mailgun)
   ↓
4. Client → POST /api/upload/profile-image
   ↓
5. UploadController → CloudinaryService
   ↓
6. CloudinaryService → Cloudinary API
   ↓
7. Restituisce URL immagine
   ↓
8. Client → PUT /api/users/me { profileImage: url }
   ↓
9. UserService salva URL nel database
```

### Scenario: Creazione Contratto con Notifica

```
1. Manager → POST /api/contratti
   ↓
2. ContrattoService crea contratto
   ↓
3. ContrattoService genera rate automaticamente
   ↓
4. ContrattoService invia notifica email (Mailgun)
   ↓
5. Locatario riceve email con dettagli contratto
```

---

## 🧪 Testing Integrazioni

### Mock per Test

Nei test, le integrazioni esterne vengono **mockate**:

```java
@MockBean
private CloudinaryService cloudinaryService;

@MockBean
private MailgunService mailgunService;

@Test
void testRegisterUser() {
    when(cloudinaryService.uploadImage(any())).thenReturn("https://example.com/image.jpg");
    when(mailgunService.sendWelcomeEmail(anyString(), anyString())).thenReturn(null);
    
    // Test registrazione...
}
```

### Disabilitare in Test

**application-test.properties**:
```properties
# Disabilita servizi esterni (usa mock)
cloudinary.enabled=false
mailgun.enabled=false
```

---

## 📝 Note Finali

### Dipendenze Esterne

Entrambe le integrazioni sono **opzionali** per il funzionamento base:
- **Cloudinary**: Se fallisce, l'upload immagine fallisce (gestire errore)
- **Mailgun**: Se fallisce, l'email non viene inviata ma l'operazione principale continua

### Alternative

**Cloudinary Alternatives**:
- AWS S3 + CloudFront
- Google Cloud Storage
- Azure Blob Storage
- Self-hosted (MinIO)

**Mailgun Alternatives**:
- SendGrid
- Amazon SES
- Postmark
- Self-hosted (Postfix)

---

Per dettagli su configurazione, consulta [CONFIGURAZIONE.md](./CONFIGURAZIONE.md).

