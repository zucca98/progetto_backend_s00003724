package com.epicode.Progetto_Backend.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.epicode.Progetto_Backend.config.CloudinaryProperties;

/**
 * CloudinaryService - Servizio per l'upload di immagini su Cloudinary.
 * 
 * Gestisce l'upload di immagini (principalmente immagini profilo utente)
 * sul servizio Cloudinary, un CDN per immagini e video.
 * 
 * Funzionalità:
 * - Upload immagini con validazione automatica del formato
 * - Organizzazione in cartelle (profile_images)
 * - Restituzione dell'URL pubblico dell'immagine caricata
 * 
 * Configurazione:
 * - Le credenziali (cloudName, apiKey, apiSecret) vengono recuperate da CloudinaryProperties
 * - Le immagini vengono caricate nella cartella "profile_images"
 * - Il tipo di risorsa viene rilevato automaticamente ("auto")
 * 
 * Utilizzato da:
 * - UploadController per l'endpoint di upload
 * - UserController per l'aggiornamento immagine profilo
 * 
 * @see com.epicode.Progetto_Backend.config.CloudinaryProperties
 * @see com.epicode.Progetto_Backend.controller.UploadController
 */
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Costruttore che inizializza Cloudinary con le credenziali da CloudinaryProperties.
     * 
     * @param cloudinaryProperties Proprietà di configurazione Cloudinary
     */
    public CloudinaryService(CloudinaryProperties cloudinaryProperties) {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryProperties.getCloudName(),
                "api_key", cloudinaryProperties.getApiKey(),
                "api_secret", cloudinaryProperties.getApiSecret()
        ));
    }
    
    /**
     * Carica un'immagine su Cloudinary e restituisce l'URL pubblico.
     * 
     * Il file viene caricato nella cartella "profile_images" su Cloudinary.
     * Il tipo di risorsa viene rilevato automaticamente (immagine, video, etc.).
     * 
     * @param file File immagine da caricare (MultipartFile)
     * @return URL pubblico dell'immagine caricata (secure_url)
     * @throws IOException se si verifica un errore durante l'upload
     */
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file) throws IOException {
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "profile_images",
                        "resource_type", "auto"
                ));
        return (String) uploadResult.get("secure_url");
    }
}
