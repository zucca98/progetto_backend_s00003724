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
 * Negozio - Sottoclasse di Immobile per immobili commerciali (negozi).
 * 
 * Rappresenta un negozio con caratteristiche specifiche: vetrine e superficie magazzino.
 * Utilizza ereditarietà JPA JOINED con PrimaryKeyJoinColumn per collegarsi alla
 * tabella principale "immobile" tramite immobile_id.
 * 
 * Campi specifici:
 * - vetrine: Numero di vetrine del negozio
 * - magazzinoMq: Metri quadri del magazzino
 * 
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see com.epicode.Progetto_Backend.entity.TipoImmobile#NEGOZIO
 */
@Entity
@Table(name = "negozio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "immobile_id")
public class Negozio extends Immobile {
    
    /** Numero di vetrine del negozio */
    @Column(nullable = false)
    private Integer vetrine;
    
    /** Superficie del magazzino in metri quadri */
    @Column(name = "magazzino_mq", nullable = false)
    private Double magazzinoMq;
}
