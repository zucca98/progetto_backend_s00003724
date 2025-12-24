package com.epicode.Progetto_Backend.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.epicode.Progetto_Backend.service.CloudinaryService;

/**
 * UploadController - Controller REST per l'upload di file.
 * 
 * Gestisce l'upload di immagini su Cloudinary, principalmente per le immagini profilo utente.
 * 
 * Funzionalità:
 * - Upload immagini profilo su Cloudinary
 * - Validazione del file (formato, dimensione)
 * - Restituzione dell'URL dell'immagine caricata
 * 
 * Caratteristiche:
 * - I file vengono caricati nella cartella "profile_images" su Cloudinary
 * - L'URL restituito può essere utilizzato per aggiornare il campo profileImage dell'utente
 * - Richiede autenticazione (qualsiasi utente autenticato può caricare immagini)
 * 
 * @see com.epicode.Progetto_Backend.service.CloudinaryService
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    /**
     * Carica un'immagine profilo su Cloudinary.
     * 
     * Il metodo:
     * 1. Riceve il file immagine tramite MultipartFile
     * 2. Valida il file (formato e dimensione)
     * 3. Carica il file su Cloudinary tramite CloudinaryService
     * 4. Restituisce l'URL pubblico dell'immagine caricata
     * 
     * L'URL restituito può essere utilizzato per aggiornare il campo profileImage
     * dell'utente tramite PUT /api/users/me/profile-image
     * 
     * @param file File immagine da caricare (MultipartFile)
     * @return Mappa con la chiave "url" contenente l'URL dell'immagine caricata
     * @throws RuntimeException se l'upload fallisce
     */
    @PostMapping("/profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        logger.info("Richiesta upload immagine profilo. Nome file: {}, Dimensione: {} bytes", 
                file.getOriginalFilename(), file.getSize());
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            logger.info("Upload immagine profilo completato con successo. URL: {}", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Errore durante l'upload dell'immagine: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante l'upload dell'immagine: " + e.getMessage());
        }
    }
}
