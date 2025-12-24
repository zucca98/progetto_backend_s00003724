package com.epicode.Progetto_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.epicode.Progetto_Backend.entity.Locatario;

/**
 * LocatarioRepository - Repository JPA per l'entità Locatario.
 * 
 * Estende JpaRepository fornendo operazioni CRUD standard e metodi di query personalizzati.
 * 
 * Ottimizzazioni:
 * - Utilizza @EntityGraph per evitare problemi N+1 quando si accede alle relazioni
 * - Le query personalizzate utilizzano JOIN FETCH per caricare le relazioni in modo efficiente
 * 
 * Note importanti:
 * - Le manutenzioni non sono incluse negli EntityGraph per evitare MultipleBagFetchException
 *   (Hibernate non può fare fetch simultaneo di due collezioni List)
 * - Le manutenzioni possono essere caricate separatamente se necessario
 * 
 * Metodi disponibili:
 * - findByCf: Trova un locatario per codice fiscale (univoco)
 * - findByUserId: Trova un locatario per ID utente (relazione one-to-one)
 * - findLocatariConContrattiLunghiDurata: Query personalizzata per locatari con contratti > 2 anni
 * 
 * @see com.epicode.Progetto_Backend.entity.Locatario
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see org.springframework.data.jpa.repository.EntityGraph
 */
@Repository
public interface LocatarioRepository extends JpaRepository<Locatario, Long> {
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a contratti e user.
     * Nota: manutenzioni non è inclusa per evitare MultipleBagFetchException (Hibernate non può
     * fare fetch simultaneo di due collezioni List). Le manutenzioni possono essere caricate
     * separatamente se necessario.
     */
    @EntityGraph(attributePaths = {"contratti", "user"})
    @Override
    @NonNull
    Page<Locatario> findAll(@NonNull Pageable pageable);
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a contratti e user.
     * Nota: manutenzioni non è inclusa per evitare MultipleBagFetchException.
     */
    @EntityGraph(attributePaths = {"contratti", "user"})
    @Override
    @NonNull
    Optional<Locatario> findById(@NonNull Long id);
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a contratti e user.
     * Nota: manutenzioni non è inclusa per evitare MultipleBagFetchException.
     */
    @EntityGraph(attributePaths = {"contratti", "user"})
    Optional<Locatario> findByCf(String cf);
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a contratti e user.
     * Nota: manutenzioni non è inclusa per evitare MultipleBagFetchException.
     */
    @EntityGraph(attributePaths = {"contratti", "user"})
    Optional<Locatario> findByUserId(Long userId);
    
    /**
     * Query 1: Trova tutti i locatari con almeno un contratto con durata maggiore di 2 anni.
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a contratti e user.
     * Nota: manutenzioni non è inclusa per evitare MultipleBagFetchException.
     * 
     * Utilizzato per identificare i clienti con contratti di lunga durata.
     * 
     * @return Lista di locatari che hanno almeno un contratto con durataAnni > 2
     */
    @Query("SELECT DISTINCT l FROM Locatario l " +
           "LEFT JOIN FETCH l.contratti " +
           "LEFT JOIN FETCH l.user " +
           "JOIN l.contratti c WHERE c.durataAnni > 2")
    List<Locatario> findLocatariConContrattiLunghiDurata();
}
