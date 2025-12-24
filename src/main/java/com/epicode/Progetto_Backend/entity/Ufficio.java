package com.epicode.Progetto_Backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Ufficio - Sottoclasse di Immobile per immobili commerciali/terziari (uffici).
 * 
 * Rappresenta un ufficio con caratteristiche specifiche: posti di lavoro e sale riunioni.
 * Utilizza ereditarietà JPA JOINED con PrimaryKeyJoinColumn per collegarsi alla
 * tabella principale "immobile" tramite immobile_id.
 * 
 * Campi specifici:
 * - postiLavoro: Numero di posti di lavoro disponibili
 * - saleRiunioni: Numero di sale riunioni
 * 
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see com.epicode.Progetto_Backend.entity.TipoImmobile#UFFICIO
 */
@Entity
@Table(name = "ufficio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "immobile_id")
public class Ufficio extends Immobile {
    
    /** Numero di posti di lavoro disponibili nell'ufficio */
    @Column(name = "posti_lavoro", nullable = false)
    private Integer postiLavoro;
    
    /** Numero di sale riunioni nell'ufficio */
    @Column(name = "sale_riunioni", nullable = false)
    private Integer saleRiunioni;
}
