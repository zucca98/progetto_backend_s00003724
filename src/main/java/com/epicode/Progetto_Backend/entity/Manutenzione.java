package com.epicode.Progetto_Backend.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Manutenzione - Entità per le manutenzioni effettuate sugli immobili.
 * 
 * Rappresenta una manutenzione effettuata su un immobile da parte o per conto di un locatario.
 * Il tipo di manutenzione può essere "ORDINARIA" o "STRAORDINARIA" (default).
 * 
 * Relazioni:
 * - Many-to-One con Immobile: Una manutenzione è su un immobile
 * - Many-to-One con Locatario: Una manutenzione è associata a un locatario
 * 
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see com.epicode.Progetto_Backend.entity.Locatario
 */
@Entity
@Table(name = "manutenzione")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manutenzione {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Immobile su cui è stata effettuata la manutenzione */
    @ManyToOne
    @JoinColumn(name = "immobile_id", nullable = false)
    private Immobile immobile;
    
    /** Locatario associato alla manutenzione */
    @ManyToOne
    @JoinColumn(name = "locatario_id", nullable = false)
    private Locatario locatario;
    
    /** Data in cui è stata effettuata la manutenzione */
    @Column(name = "data_man", nullable = false)
    private LocalDate dataMan;
    
    /** Importo della manutenzione in euro */
    @Column(nullable = false)
    private Double importo;
    
    /** Tipo di manutenzione: "ORDINARIA" o "STRAORDINARIA" (default: "STRAORDINARIA") */
    @Column(nullable = false)
    @Builder.Default
    private String tipo = "STRAORDINARIA";
    
    /** Descrizione dettagliata della manutenzione (opzionale, tipo TEXT) */
    @Column(columnDefinition = "TEXT")
    private String descrizione;
}
