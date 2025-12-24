package com.epicode.Progetto_Backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * MailgunProperties - Classe di configurazione per le proprietà di Mailgun.
 * 
 * Questa classe mappa le proprietà di configurazione relative al servizio Mailgun
 * utilizzato per l'invio di email di notifica (es: notifiche pagamento rate scadute).
 * 
 * Le proprietà vengono lette dal file env.properties con il prefisso "mailgun":
 * - mailgun.api-key: API Key per autenticazione con Mailgun
 * - mailgun.domain: Dominio Mailgun configurato per l'invio email
 * - mailgun.from-email: Indirizzo email mittente per le email inviate
 * 
 * Utilizzata da MailgunService per configurare il client Mailgun e inviare email.
 */
@Data
@Component
@ConfigurationProperties(prefix = "mailgun")
public class MailgunProperties {
    
    /** API Key per l'autenticazione con il servizio Mailgun */
    private String apiKey;
    
    /** Dominio Mailgun configurato per l'invio delle email */
    private String domain;
    
    /** Indirizzo email del mittente utilizzato per tutte le email inviate */
    private String fromEmail;
}

