package com.epicode.Progetto_Backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epicode.Progetto_Backend.entity.User;

/**
 * UserRepository - Repository JPA per l'entità User.
 * 
 * Estende JpaRepository fornendo operazioni CRUD standard e metodi di query personalizzati.
 * 
 * Metodi disponibili:
 * - findByEmail: Trova un utente per email (utilizzato per login e autenticazione)
 * - existsByEmail: Verifica se esiste un utente con una determinata email (validazione univocità)
 * 
 * L'email è univoca nel sistema e viene utilizzata come username per l'autenticazione.
 * 
 * @see com.epicode.Progetto_Backend.entity.User
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Trova un utente per email.
     * 
     * Utilizzato principalmente per:
     * - Autenticazione (login)
     * - Recupero utente corrente (me)
     * - Validazione esistenza utente
     * 
     * @param email Email dell'utente (univoca)
     * @return Optional contenente l'utente se trovato, empty altrimenti
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica se esiste un utente con la specifica email.
     * 
     * Utilizzato per validare l'univocità dell'email durante la registrazione
     * o l'aggiornamento del profilo.
     * 
     * @param email Email da verificare
     * @return true se esiste un utente con questa email, false altrimenti
     */
    Boolean existsByEmail(String email);
}
