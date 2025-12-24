package com.epicode.Progetto_Backend.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.epicode.Progetto_Backend.dto.UserUpdateDTO;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.service.CloudinaryService;
import com.epicode.Progetto_Backend.service.UserService;

import jakarta.validation.Valid;

/**
 * UserController - Controller REST per la gestione degli utenti.
 * 
 * Gestisce le operazioni CRUD sugli utenti del sistema:
 * - Visualizzazione utenti (lista, per ID, utente corrente)
 * - Aggiornamento profilo utente
 * - Aggiornamento immagine profilo (upload su Cloudinary)
 * - Gestione ruoli utente (solo ADMIN)
 * - Eliminazione utenti (solo ADMIN)
 * 
 * Endpoint speciali:
 * - GET /api/users/me: Restituisce l'utente corrente autenticato
 * - PUT /api/users/me: Aggiorna il profilo dell'utente corrente
 * - PUT /api/users/me/profile-image: Aggiorna l'immagine profilo
 * 
 * Autorizzazioni:
 * - ADMIN: Accesso completo a tutte le operazioni
 * - Utente autenticato: Può visualizzare e aggiornare solo il proprio profilo
 * 
 * @see com.epicode.Progetto_Backend.service.UserService
 * @see com.epicode.Progetto_Backend.service.CloudinaryService
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    /**
     * Ottiene tutti gli utenti del sistema (paginated).
     * 
     * @return Lista paginata di tutti gli utenti
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    /**
     * Ottiene un utente specifico per ID.
     * 
     * @param id ID dell'utente da recuperare
     * @return Utente con l'ID specificato
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    /**
     * Ottiene l'utente corrente autenticato.
     * 
     * Utilizza l'email dell'utente autenticato (da Authentication) per recuperare
     * i dati completi dell'utente dal database.
     * 
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @return Utente corrente completo con ruoli e informazioni
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
    
    /**
     * Aggiorna il profilo dell'utente corrente autenticato.
     * 
     * Permette di aggiornare nome, cognome e immagine profilo.
     * L'utente può aggiornare solo i propri dati.
     * 
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @param request DTO con i campi da aggiornare (nome, cognome, profileImage)
     * @return Utente aggiornato
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserUpdateDTO request) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);
        return ResponseEntity.ok(userService.updateUser(currentUser.getId(), request));
    }
    
    /**
     * Aggiorna l'immagine profilo dell'utente corrente caricandola su Cloudinary.
     * 
     * Il metodo:
     * 1. Carica il file immagine su Cloudinary tramite CloudinaryService
     * 2. Ottiene l'URL dell'immagine caricata
     * 3. Aggiorna il campo profileImage dell'utente con l'URL
     * 
     * Il file viene caricato nella cartella "profile_images" su Cloudinary.
     * 
     * @param authentication Oggetto Spring Security con i dati dell'utente autenticato
     * @param file File immagine da caricare (MultipartFile)
     * @return Utente con l'URL dell'immagine profilo aggiornato
     * @throws RuntimeException se l'upload fallisce
     */
    @PutMapping("/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateProfileImage(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            String email = authentication.getName();
            return ResponseEntity.ok(userService.updateProfileImageByEmail(email, imageUrl));
        } catch (IOException e) {
            throw new RuntimeException("Errore durante l'upload dell'immagine: " + e.getMessage());
        }
    }
    
    /**
     * Aggiorna un utente specifico (solo ADMIN).
     * 
     * @param id ID dell'utente da aggiornare
     * @param request DTO con i campi da aggiornare
     * @return Utente aggiornato
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
    
    /**
     * Elimina un utente dal sistema (solo ADMIN).
     * 
     * @param id ID dell'utente da eliminare
     * @return 204 No Content se l'eliminazione è riuscita
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Aggiorna i ruoli di un utente (solo ADMIN).
     * 
     * Permette di assegnare/rimuovere ruoli a un utente.
     * I ruoli devono essere nel formato "ROLE_*" (es: "ROLE_ADMIN", "ROLE_MANAGER").
     * 
     * @param id ID dell'utente
     * @param roles Set di nomi ruoli da assegnare all'utente
     * @return Utente con i ruoli aggiornati
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRoles(
            @PathVariable Long id,
            @RequestBody Set<String> roles) {
        return ResponseEntity.ok(userService.updateUserRoles(id, roles));
    }

    /**
     * Ottiene tutti i ruoli disponibili nel sistema (solo ADMIN).
     * 
     * @return Lista di tutti i ruoli (ROLE_ADMIN, ROLE_MANAGER, ROLE_LOCATARIO)
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }
}
