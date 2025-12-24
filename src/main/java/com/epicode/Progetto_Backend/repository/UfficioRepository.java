package com.epicode.Progetto_Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epicode.Progetto_Backend.entity.Ufficio;

/**
 * UfficioRepository - Repository JPA per l'entità Ufficio.
 * 
 * Estende JpaRepository fornendo operazioni CRUD standard e metodi di query personalizzati
 * per gli uffici (sottotipo di Immobile).
 * 
 * Metodi disponibili:
 * - findByPostiLavoroGreaterThanEqual: Trova uffici con almeno un numero di posti di lavoro
 * - findBySaleRiunioniGreaterThanEqual: Trova uffici con almeno un numero di sale riunioni
 * 
 * @see com.epicode.Progetto_Backend.entity.Ufficio
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface UfficioRepository extends JpaRepository<Ufficio, Long> {
    
    /**
     * Trova tutti gli uffici con almeno un numero di posti di lavoro.
     * 
     * Utile per filtrare uffici in base al numero minimo di posti di lavoro richiesto.
     * 
     * @param postiLavoro Numero minimo di posti di lavoro
     * @return Lista di uffici con postiLavoro >= parametro
     */
    List<Ufficio> findByPostiLavoroGreaterThanEqual(Integer postiLavoro);
    
    /**
     * Trova tutti gli uffici con almeno un numero di sale riunioni.
     * 
     * Utile per filtrare uffici in base al numero minimo di sale riunioni richiesto.
     * 
     * @param saleRiunioni Numero minimo di sale riunioni
     * @return Lista di uffici con saleRiunioni >= parametro
     */
    List<Ufficio> findBySaleRiunioniGreaterThanEqual(Integer saleRiunioni);
}

