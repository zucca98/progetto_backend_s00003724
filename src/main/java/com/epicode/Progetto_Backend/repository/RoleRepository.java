package com.epicode.Progetto_Backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epicode.Progetto_Backend.entity.Role;

/**
 * RoleRepository - Repository JPA per l'entità Role.
 * 
 * Estende JpaRepository fornendo operazioni CRUD standard e metodi di query personalizzati.
 * 
 * Metodi disponibili:
 * - findByName: Trova un ruolo per nome (es: "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_LOCATARIO")
 * 
 * I ruoli seguono il prefisso "ROLE_" come richiesto da Spring Security.
 * Il nome del ruolo è univoco nel sistema.
 * 
 * @see com.epicode.Progetto_Backend.entity.Role
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Trova un ruolo per nome.
     * 
     * Utilizzato per:
     * - Assegnazione ruoli agli utenti
     * - Verifica esistenza di un ruolo
     * - Recupero ruolo per nome durante l'inizializzazione
     * 
     * @param name Nome del ruolo (es: "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_LOCATARIO")
     * @return Optional contenente il ruolo se trovato, empty altrimenti
     */
    Optional<Role> findByName(String name);
}
