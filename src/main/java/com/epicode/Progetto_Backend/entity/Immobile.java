package com.epicode.Progetto_Backend.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immobile - Entità base per gli immobili gestiti dal sistema.
 * 
 * Classe base astratta per gli immobili con ereditarietà JPA di tipo JOINED.
 * Utilizza InheritanceType.JOINED per creare una tabella principale "immobile"
 * e tabelle separate per ogni sottotipo (appartamento, negozio, ufficio).
 * 
 * Sottoclassi:
 * - Appartamento: Immobili residenziali (piano, numCamere)
 * - Negozio: Immobili commerciali (vetrine, magazzinoMq)
 * - Ufficio: Immobili terziari (postiLavoro, saleRiunioni)
 * 
 * Relazioni:
 * - One-to-Many con Contratto: Un immobile può avere più contratti (storico)
 * - One-to-Many con Manutenzione: Un immobile può avere più manutenzioni
 * 
 * @see com.epicode.Progetto_Backend.entity.Appartamento
 * @see com.epicode.Progetto_Backend.entity.Negozio
 * @see com.epicode.Progetto_Backend.entity.Ufficio
 * @see com.epicode.Progetto_Backend.entity.TipoImmobile
 */
@Entity
@Table(name = "immobile")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Immobile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Indirizzo completo dell'immobile */
    @Column(nullable = false)
    private String indirizzo;
    
    /** Città dell'immobile */
    @Column(nullable = false)
    private String citta;
    
    /** Superficie totale in metri quadri */
    @Column(nullable = false)
    private Double superficie;
    
    /** Tipo di immobile (APPARTAMENTO, NEGOZIO, UFFICIO) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoImmobile tipo;
    
    /** Lista dei contratti associati a questo immobile (non serializzata in JSON) */
    @OneToMany(mappedBy = "immobile", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Contratto> contratti = new ArrayList<>();
    
    /** Lista delle manutenzioni effettuate su questo immobile (non serializzata in JSON) */
    @OneToMany(mappedBy = "immobile", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Manutenzione> manutenzioni = new ArrayList<>();
}
