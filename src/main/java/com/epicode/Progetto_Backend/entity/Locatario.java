package com.epicode.Progetto_Backend.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Locatario - Entità per i locatari (affittuari) degli immobili.
 * 
 * Rappresenta un locatario con dati anagrafici completi.
 * Ogni locatario è associato a un User tramite relazione one-to-one,
 * permettendo l'accesso al sistema tramite login.
 * 
 * Relazioni:
 * - One-to-One con User: Ogni locatario ha un account utente
 * - One-to-Many con Contratto: Un locatario può avere più contratti (storico)
 * - One-to-Many con Manutenzione: Un locatario può avere più manutenzioni
 * 
 * Il codice fiscale (cf) deve essere univoco nel sistema.
 * 
 * @see com.epicode.Progetto_Backend.entity.User
 * @see com.epicode.Progetto_Backend.entity.Contratto
 * @see com.epicode.Progetto_Backend.entity.Manutenzione
 */
@Entity
@Table(name = "locatario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Locatario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Nome del locatario */
    @Column(nullable = false)
    private String nome;
    
    /** Cognome del locatario */
    @Column(nullable = false)
    private String cognome;
    
    /** Codice fiscale (deve essere univoco nel sistema) */
    @Column(nullable = false, unique = true)
    private String cf; // Codice Fiscale univoco
    
    /** Indirizzo completo del locatario */
    @Column(nullable = false)
    private String indirizzo;
    
    /** Numero di telefono del locatario */
    @Column(nullable = false)
    private String telefono;
    
    /** Utente associato a questo locatario (relazione one-to-one obbligatoria) */
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonIgnore
    private User user;
    
    /** Lista dei contratti del locatario (non serializzata in JSON) */
    @OneToMany(mappedBy = "locatario", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Contratto> contratti = new ArrayList<>();
    
    /** Lista delle manutenzioni associate al locatario (non serializzata in JSON) */
    @OneToMany(mappedBy = "locatario", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Manutenzione> manutenzioni = new ArrayList<>();
}
