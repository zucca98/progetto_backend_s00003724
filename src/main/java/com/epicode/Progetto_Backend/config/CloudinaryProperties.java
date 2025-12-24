package com.epicode.Progetto_Backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * CloudinaryProperties - Classe di configurazione per le proprietà di Cloudinary.
 * 
 * Questa classe mappa le proprietà di configurazione relative al servizio Cloudinary
 * per l'upload e la gestione delle immagini del profilo utente.
 * 
 * Le proprietà vengono lette dal file application.properties con il prefisso "cloudinary":
 * - cloudinary.cloud-name: Nome del cloud Cloudinary
 * - cloudinary.api-key: API Key per autenticazione Cloudinary
 * - cloudinary.api-secret: API Secret per autenticazione Cloudinary
 * 
 * Utilizzata da CloudinaryService per configurare il client Cloudinary.
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {
    
    /** Nome del cloud Cloudinary (configurato nel dashboard Cloudinary) */
    private String cloudName;
    
    /** API Key per l'autenticazione con Cloudinary */
    private String apiKey;
    
    /** API Secret per l'autenticazione con Cloudinary */
    private String apiSecret;
}

