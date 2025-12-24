package com.epicode.Progetto_Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epicode.Progetto_Backend.entity.Appartamento;

/**
 * AppartamentoRepository - Repository JPA per l'entità Appartamento.
 * 
 * Estende JpaRepository fornendo operazioni CRUD standard e metodi di query personalizzati
 * per gli appartamenti (sottotipo di Immobile).
 * 
 * Metodi disponibili:
 * - findByPiano: Trova appartamenti su un piano specifico
 * - findByNumCamereGreaterThanEqual: Trova appartamenti con almeno un numero di camere
 * 
 * @see com.epicode.Progetto_Backend.entity.Appartamento
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface AppartamentoRepository extends JpaRepository<Appartamento, Long> {
    
    /**
     * Trova tutti gli appartamenti su un piano specifico.
     * 
     * @param piano Numero del piano
     * @return Lista di appartamenti sul piano specificato
     */
    List<Appartamento> findByPiano(Integer piano);
    
    /**
     * Trova tutti gli appartamenti con almeno un numero di camere.
     * 
     * Utile per filtrare appartamenti in base al numero minimo di camere richiesto.
     * 
     * @param numCamere Numero minimo di camere
     * @return Lista di appartamenti con numCamere >= parametro
     */
    List<Appartamento> findByNumCamereGreaterThanEqual(Integer numCamere);
}

