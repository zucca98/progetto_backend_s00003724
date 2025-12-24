package com.epicode.Progetto_Backend.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.epicode.Progetto_Backend.config.MailgunProperties;

import jakarta.annotation.PostConstruct;
import kong.unirest.Config;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Servizio per l'invio di email tramite Mailgun API.
 * Supporta invio di email in formato testo e HTML con gestione asincrona.
 */
@Service
public class MailgunService {

    private static final Logger logger = LoggerFactory.getLogger(MailgunService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private MailgunProperties mailgunProperties;
    
    /**
     * Verifica la configurazione Mailgun all'avvio dell'applicazione.
     * Configura anche Unirest per usare Java HttpClient nativo invece di Apache HttpClient
     * per evitare conflitti di versioni.
     */
    @PostConstruct
    public void init() {
        logger.info("Inizializzazione MailgunService...");
        try {
            // Configura Unirest per evitare problemi con Apache HttpClient
            try {
                Config config = Unirest.config();
                config.concurrency(10, 5); // Limita le connessioni concorrenti
                logger.info("Unirest configurato");
            } catch (Exception e) {
                logger.warn("Impossibile configurare Unirest: {}", e.getMessage());
            }
            
            validateMailgunProperties();
            logger.info("MailgunService inizializzato correttamente");
        } catch (Exception e) {
            logger.error("ERRORE CRITICO: MailgunService non configurato correttamente! Le email non verranno inviate.", e);
        }
    }
    
    /**
     * Verifica che le proprietà Mailgun siano configurate correttamente.
     */
    private void validateMailgunProperties() {
        if (mailgunProperties == null) {
            logger.error("MailgunProperties non è stato inizializzato!");
            throw new IllegalStateException("MailgunProperties non configurato");
        }
        
        if (mailgunProperties.getApiKey() == null || mailgunProperties.getApiKey().isEmpty()) {
            logger.error("Mailgun API Key non configurata!");
            throw new IllegalStateException("Mailgun API Key non configurata");
        }
        
        if (mailgunProperties.getDomain() == null || mailgunProperties.getDomain().isEmpty()) {
            logger.error("Mailgun Domain non configurato!");
            throw new IllegalStateException("Mailgun Domain non configurato");
        }
        
        if (mailgunProperties.getFromEmail() == null || mailgunProperties.getFromEmail().isEmpty()) {
            logger.error("Mailgun From Email non configurata!");
            throw new IllegalStateException("Mailgun From Email non configurata");
        }
        
        logger.info("Mailgun configurato correttamente - Domain: {}, From: {}", 
                mailgunProperties.getDomain(), mailgunProperties.getFromEmail());
    }
    
    // ============================================================================
    // METODI BASE DI INVIO EMAIL
    // ============================================================================
    
    /**
     * Invia un'email in formato testo semplice.
     */
    public void sendEmail(String to, String subject, String text) {
        logger.info("Invio email a: {} - Oggetto: {}", to, subject);
        
        try {
            validateMailgunProperties();
            
            String apiUrl = getApiUrl();
            logger.debug("URL Mailgun: {}", apiUrl);
            logger.debug("API Key presente: {}", mailgunProperties.getApiKey() != null && !mailgunProperties.getApiKey().isEmpty());
            
            HttpResponse<String> response = Unirest.post(apiUrl)
                    .basicAuth("api", mailgunProperties.getApiKey())
                    .field("from", mailgunProperties.getFromEmail())
                    .field("to", to)
                    .field("subject", subject)
                    .field("text", text)
                    .asString();

            logger.debug("Risposta Mailgun - Status: {}, Body: {}", response.getStatus(), response.getBody());
            handleResponse(response, to, subject);
        } catch (Exception e) {
            logger.error("Errore durante l'invio email a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Errore nell'invio email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Invia un'email in formato HTML.
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        logger.info("Invio email HTML a: {} - Oggetto: {}", to, subject);
        
        try {
            validateMailgunProperties();
            
            String apiUrl = getApiUrl();
            logger.debug("URL Mailgun: {}", apiUrl);
            
            HttpResponse<String> response = Unirest.post(apiUrl)
                    .basicAuth("api", mailgunProperties.getApiKey())
                    .field("from", mailgunProperties.getFromEmail())
                    .field("to", to)
                    .field("subject", subject)
                    .field("html", htmlContent)
                    .asString();
            
            logger.debug("Risposta Mailgun - Status: {}, Body: {}", response.getStatus(), response.getBody());

            handleResponse(response, to, subject);
        } catch (Exception e) {
            logger.error("Errore durante l'invio email HTML a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Errore nell'invio email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Invia un'email in modo asincrono (non blocca il thread principale).
     */
    @Async
    public void sendEmailAsync(String to, String subject, String text) {
        sendEmail(to, subject, text);
    }
    
    /**
     * Invia un'email HTML in modo asincrono.
     */
    @Async
    public void sendHtmlEmailAsync(String to, String subject, String htmlContent) {
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    // ============================================================================
    // EMAIL DI BENVENUTO
    // ============================================================================
    
    /**
     * Invia un'email di benvenuto a un nuovo utente registrato.
     */
    @Async
    public void sendWelcomeEmail(String userEmail, String userName) {
        logger.info("Invio email di benvenuto a: {}", userEmail);
        
        String subject = "🏠 Benvenuto nella Cooperativa Immobiliare";
        String htmlContent = buildHtmlTemplate(
            "Benvenuto, " + userName + "!",
            """
            <p>Grazie per esserti registrato sulla nostra piattaforma.</p>
            <p>Il tuo account è stato creato con successo e ora puoi accedere a tutti i nostri servizi:</p>
            <ul>
                <li>Consultare i tuoi contratti di affitto</li>
                <li>Visualizzare le rate e lo storico pagamenti</li>
                <li>Richiedere interventi di manutenzione</li>
                <li>Gestire il tuo profilo</li>
            </ul>
            <p>Se hai domande, non esitare a contattarci.</p>
            """
        );
        
        try {
            sendHtmlEmail(userEmail, subject, htmlContent);
            logger.info("Email di benvenuto inviata con successo a: {}", userEmail);
        } catch (Exception e) {
            logger.error("ERRORE nell'invio email di benvenuto a {}: {}", userEmail, e.getMessage(), e);
            // Non rilanciamo l'eccezione per non bloccare la registrazione, ma logghiamo l'errore completo
        }
    }
    
    // ============================================================================
    // EMAIL CONTRATTI
    // ============================================================================
    
    /**
     * Notifica la creazione di un nuovo contratto di affitto.
     */
    @Async
    public void sendContractNotification(String userEmail, String locatarioName, String immobileIndirizzo) {
        logger.info("Invio notifica nuovo contratto a: {}", userEmail);
        
        String subject = "📝 Nuovo Contratto di Affitto";
        String htmlContent = buildHtmlTemplate(
            "Nuovo Contratto Registrato",
            String.format("""
            <p>Gentile <strong>%s</strong>,</p>
            <p>Le confermiamo la registrazione del nuovo contratto di affitto per l'immobile situato in:</p>
            <div style="background-color: #f5f5f5; padding: 15px; border-radius: 8px; margin: 20px 0;">
                <strong>📍 %s</strong>
            </div>
            <p>Può consultare tutti i dettagli del contratto e le rate associate accedendo alla sua area riservata.</p>
            """,
            locatarioName,
            immobileIndirizzo)
        );
        
        try {
            sendHtmlEmail(userEmail, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Errore nell'invio notifica contratto a {}: {}", userEmail, e.getMessage());
        }
    }
    
    /**
     * Notifica la scadenza di un contratto.
     */
    @Async
    public void sendContractExpirationNotification(String userEmail, String locatarioName, 
            String immobileIndirizzo, LocalDate dataScadenza) {
        logger.info("Invio notifica scadenza contratto a: {}", userEmail);
        
        String subject = "⚠️ Contratto in Scadenza";
        String htmlContent = buildHtmlTemplate(
            "Contratto in Scadenza",
            String.format("""
            <p>Gentile <strong>%s</strong>,</p>
            <p>La informiamo che il contratto di affitto per l'immobile in <strong>%s</strong> 
            scadrà il <strong>%s</strong>.</p>
            <p>Per il rinnovo o per ulteriori informazioni, La preghiamo di contattarci.</p>
            """,
            locatarioName,
            immobileIndirizzo,
            dataScadenza.format(DATE_FORMATTER))
        );
        
        try {
            sendHtmlEmail(userEmail, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Errore nell'invio notifica scadenza contratto a {}: {}", userEmail, e.getMessage());
        }
    }
    
    // ============================================================================
    // EMAIL RATE E PAGAMENTI
    // ============================================================================
    
    /**
     * Invia promemoria per rata in scadenza.
     */
    @Async
    public void sendPaymentReminderEmail(String userEmail, String locatarioName, 
            int numeroRata, Double importo, LocalDate dataScadenza, String immobileIndirizzo) {
        logger.info("Invio promemoria pagamento rata a: {}", userEmail);
        
        String subject = "🔔 Promemoria Pagamento Rata";
        String htmlContent = buildHtmlTemplate(
            "Promemoria Pagamento",
            String.format("""
            <p>Gentile <strong>%s</strong>,</p>
            <p>Le ricordiamo che la <strong>rata n. %d</strong> del suo contratto di affitto 
            per l'immobile in <strong>%s</strong> è in scadenza.</p>
            <div style="background-color: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107;">
                <p style="margin: 0;"><strong>Importo:</strong> € %.2f</p>
                <p style="margin: 10px 0 0 0;"><strong>Scadenza:</strong> %s</p>
            </div>
            <p>La preghiamo di provvedere al pagamento entro la data indicata.</p>
            """,
            locatarioName,
            numeroRata,
            immobileIndirizzo,
            importo,
            dataScadenza.format(DATE_FORMATTER))
        );
        
        try {
            sendHtmlEmail(userEmail, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Errore nell'invio promemoria pagamento a {}: {}", userEmail, e.getMessage());
        }
    }
    
    /**
     * Conferma l'avvenuto pagamento di una rata.
     */
    @Async
    public void sendPaymentConfirmationEmail(String userEmail, String locatarioName,
            int numeroRata, Double importo, String immobileIndirizzo) {
        logger.info("Invio conferma pagamento rata a: {}", userEmail);
        
        String subject = "✅ Conferma Pagamento Rata";
        String htmlContent = buildHtmlTemplate(
            "Pagamento Confermato",
            String.format("""
            <p>Gentile <strong>%s</strong>,</p>
            <p>Confermiamo la ricezione del pagamento per la <strong>rata n. %d</strong> 
            del suo contratto di affitto per l'immobile in <strong>%s</strong>.</p>
            <div style="background-color: #d4edda; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                <p style="margin: 0;"><strong>✓ Importo ricevuto:</strong> € %.2f</p>
            </div>
            <p>Grazie per la puntualità nel pagamento.</p>
            """,
            locatarioName,
            numeroRata,
            immobileIndirizzo,
            importo)
        );
        
        try {
            sendHtmlEmail(userEmail, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Errore nell'invio conferma pagamento a {}: {}", userEmail, e.getMessage());
        }
    }
    
    /**
     * Notifica rata scaduta e non pagata.
     */
    @Async
    public void sendOverduePaymentNotification(String userEmail, String locatarioName,
            int numeroRata, Double importo, LocalDate dataScadenza, String immobileIndirizzo) {
        logger.info("Invio notifica rata scaduta a: {}", userEmail);
        
        String subject = "❌ Rata Scaduta - Sollecito Pagamento";
        String htmlContent = buildHtmlTemplate(
            "Sollecito Pagamento",
            String.format("""
            <p>Gentile <strong>%s</strong>,</p>
            <p>La informiamo che la <strong>rata n. %d</strong> del suo contratto di affitto 
            per l'immobile in <strong>%s</strong> risulta <strong>scaduta e non pagata</strong>.</p>
            <div style="background-color: #f8d7da; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                <p style="margin: 0;"><strong>Importo dovuto:</strong> € %.2f</p>
                <p style="margin: 10px 0 0 0;"><strong>Scadenza:</strong> %s</p>
            </div>
            <p>La preghiamo di provvedere al pagamento il prima possibile per evitare ulteriori conseguenze.</p>
            """,
            locatarioName,
            numeroRata,
            immobileIndirizzo,
            importo,
            dataScadenza.format(DATE_FORMATTER))
        );
        
        try {
            sendHtmlEmail(userEmail, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Errore nell'invio sollecito pagamento a {}: {}", userEmail, e.getMessage());
        }
    }
    
    // ============================================================================
    // EMAIL MANUTENZIONE
    // ============================================================================
    
    /**
     * Conferma la registrazione di una richiesta di manutenzione.
     */
    @Async
    public void sendMaintenanceRequestConfirmation(String userEmail, String locatarioName,
            String immobileIndirizzo, String tipoManutenzione, String descrizione, LocalDate data) {
        logger.info("Invio conferma richiesta manutenzione a: {}", userEmail);
        
        String subject = "🔧 Richiesta Manutenzione Registrata";
        String htmlContent = buildHtmlTemplate(
            "Richiesta Manutenzione Confermata",
            String.format("""
            <p>Gentile <strong>%s</strong>,</p>
            <p>La informiamo che la sua richiesta di manutenzione è stata registrata con successo.</p>
            <div style="background-color: #e7f3ff; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007bff;">
                <p style="margin: 0;"><strong>Immobile:</strong> %s</p>
                <p style="margin: 10px 0 0 0;"><strong>Tipo:</strong> %s</p>
                <p style="margin: 10px 0 0 0;"><strong>Data:</strong> %s</p>
                <p style="margin: 10px 0 0 0;"><strong>Descrizione:</strong> %s</p>
            </div>
            <p>Sarà contattato a breve per concordare l'intervento.</p>
            """,
            locatarioName,
            immobileIndirizzo,
            tipoManutenzione,
            data.format(DATE_FORMATTER),
            descrizione != null ? descrizione : "N/A")
        );
        
        try {
            sendHtmlEmail(userEmail, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Errore nell'invio conferma manutenzione a {}: {}", userEmail, e.getMessage());
        }
    }
    
    /**
     * Notifica il completamento di un intervento di manutenzione.
     */
    @Async
    public void sendMaintenanceCompletedNotification(String userEmail, String locatarioName,
            String immobileIndirizzo, String tipoManutenzione, Double importo) {
        logger.info("Invio notifica manutenzione completata a: {}", userEmail);
        
        String subject = "✅ Manutenzione Completata";
        String htmlContent = buildHtmlTemplate(
            "Intervento di Manutenzione Completato",
            String.format("""
            <p>Gentile <strong>%s</strong>,</p>
            <p>La informiamo che l'intervento di manutenzione per l'immobile in <strong>%s</strong> 
            è stato completato.</p>
            <div style="background-color: #d4edda; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                <p style="margin: 0;"><strong>Tipo intervento:</strong> %s</p>
                <p style="margin: 10px 0 0 0;"><strong>Costo:</strong> € %.2f</p>
            </div>
            <p>Per qualsiasi chiarimento, non esiti a contattarci.</p>
            """,
            locatarioName,
            immobileIndirizzo,
            tipoManutenzione,
            importo)
        );
        
        try {
            sendHtmlEmail(userEmail, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Errore nell'invio notifica manutenzione completata a {}: {}", userEmail, e.getMessage());
        }
    }
    
    // ============================================================================
    // METODI DI UTILITY
    // ============================================================================
    
    /**
     * Costruisce l'URL dell'API Mailgun.
     */
    private String getApiUrl() {
        return "https://api.mailgun.net/v3/" + mailgunProperties.getDomain() + "/messages";
    }
    
    /**
     * Gestisce la risposta dell'API Mailgun.
     */
    private void handleResponse(HttpResponse<String> response, String to, String subject) {
        if (response.getStatus() == 200) {
            logger.info("Email inviata con successo a: {} - Oggetto: {}", to, subject);
        } else {
            logger.error("Errore Mailgun - Status: {}, Response: {}", response.getStatus(), response.getBody());
            throw new RuntimeException("Mailgun API error: " + response.getStatus() + " - " + response.getBody());
        }
    }
    
    /**
     * Costruisce un template HTML standard per le email.
     */
    private String buildHtmlTemplate(String title, String content) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="it">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">🏠 Cooperativa Immobiliare</h1>
                </div>
                <div style="background-color: #ffffff; padding: 30px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #333; margin-top: 0;">%s</h2>
                    %s
                </div>
                <div style="text-align: center; padding: 20px; color: #666; font-size: 12px;">
                    <p style="margin: 0;">Questa è un'email automatica, si prega di non rispondere.</p>
                    <p style="margin: 10px 0 0 0;">© 2025 Cooperativa Immobiliare - Tutti i diritti riservati</p>
                </div>
            </body>
            </html>
            """, title, content);
    }
}
