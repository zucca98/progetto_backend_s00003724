package com.epicode.Progetto_Backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role - Entità per i ruoli degli utenti nel sistema.
 * 
 * Rappresenta i ruoli disponibili per gli utenti (ADMIN, MANAGER, LOCATARIO).
 * Gli utenti possono avere più ruoli (many-to-many con User).
 * 
 * I nomi dei ruoli seguono il prefisso "ROLE_" come richiesto da Spring Security:
 * - ROLE_ADMIN: Accesso completo a tutte le funzionalità
 * - ROLE_MANAGER: Accesso a gestione immobili, contratti, rate (no eliminazione)
 * - ROLE_LOCATARIO: Accesso limitato ai propri dati (contratti, rate, manutenzioni)
 * 
 * @see com.epicode.Progetto_Backend.entity.User
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    
    /** ID univoco del ruolo */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Nome del ruolo (deve essere univoco): ROLE_ADMIN, ROLE_MANAGER, ROLE_LOCATARIO */
    @Column(nullable = false, unique = true)
    private String name; // ROLE_ADMIN, ROLE_MANAGER, ROLE_LOCATARIO
}
