package com.epicode.Progetto_Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epicode.Progetto_Backend.entity.Negozio;

/**
 * NegozioRepository - Repository JPA per l'entità Negozio.
 * 
 * Estende JpaRepository fornendo operazioni CRUD standard e metodi di query personalizzati
 * per i negozi (sottotipo di Immobile).
 * 
 * Metodi disponibili:
 * - findByVetrineGreaterThanEqual: Trova negozi con almeno un numero di vetrine
 * - findByMagazzinoMqGreaterThanEqual: Trova negozi con almeno una superficie magazzino
 * 
 * @see com.epicode.Progetto_Backend.entity.Negozio
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface NegozioRepository extends JpaRepository<Negozio, Long> {
    
    /**
     * Trova tutti i negozi con almeno un numero di vetrine.
     * 
     * Utile per filtrare negozi in base al numero minimo di vetrine richiesto.
     * 
     * @param vetrine Numero minimo di vetrine
     * @return Lista di negozi con vetrine >= parametro
     */
    List<Negozio> findByVetrineGreaterThanEqual(Integer vetrine);
    
    /**
     * Trova tutti i negozi con almeno una superficie magazzino.
     * 
     * Utile per filtrare negozi in base alla superficie minima del magazzino richiesta.
     * 
     * @param magazzinoMq Superficie minima del magazzino in metri quadri
     * @return Lista di negozi con magazzinoMq >= parametro
     */
    List<Negozio> findByMagazzinoMqGreaterThanEqual(Double magazzinoMq);
}

