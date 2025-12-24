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
 * Appartamento - Sottoclasse di Immobile per immobili residenziali.
 * 
 * Rappresenta un appartamento con caratteristiche specifiche: piano e numero di camere.
 * Utilizza ereditarietà JPA JOINED con PrimaryKeyJoinColumn per collegarsi alla
 * tabella principale "immobile" tramite immobile_id.
 * 
 * Campi specifici:
 * - piano: Piano dell'appartamento
 * - numCamere: Numero di camere
 * 
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see com.epicode.Progetto_Backend.entity.TipoImmobile#APPARTAMENTO
 */
@Entity
@Table(name = "appartamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "immobile_id")
public class Appartamento extends Immobile {
    
    /** Piano dell'appartamento */
    @Column(nullable = false)
    private Integer piano;
    
    /** Numero di camere dell'appartamento */
    @Column(name = "num_camere", nullable = false)
    private Integer numCamere;
}
