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

import com.epicode.Progetto_Backend.entity.Contratto;

/**
 * ContrattoRepository - Repository JPA per l'entità Contratto.
 * 
 * Estende JpaRepository fornendo operazioni CRUD standard e metodi di query personalizzati.
 * 
 * Ottimizzazioni:
 * - Utilizza @EntityGraph per evitare problemi N+1 quando si accede alle relazioni
 * - Le query personalizzate utilizzano JOIN FETCH per caricare le relazioni in modo efficiente
 * - Carica sempre rate, immobile e locatario per evitare lazy loading
 * 
 * Metodi disponibili:
 * - findByLocatarioId: Trova contratti per ID locatario
 * - findByLocatarioUserEmail: Trova contratti per email utente (tramite locatario)
 * - findByImmobileId: Trova contratti per ID immobile
 * - findContrattiConAlmenoTreRateNonPagate: Query personalizzata per contratti con morosità
 * 
 * @see com.epicode.Progetto_Backend.entity.Contratto
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see org.springframework.data.jpa.repository.EntityGraph
 */
@Repository
public interface ContrattoRepository extends JpaRepository<Contratto, Long> {
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a rate, immobile e locatario
     */
    @EntityGraph(attributePaths = {"rate", "immobile", "locatario"})
    @Override
    @NonNull
    Page<Contratto> findAll(@NonNull Pageable pageable);
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a rate, immobile e locatario
     */
    @EntityGraph(attributePaths = {"rate", "immobile", "locatario"})
    @Override
    @NonNull
    Optional<Contratto> findById(@NonNull Long id);
    
    /**
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a rate, immobile e locatario
     */
    @Query("SELECT DISTINCT c FROM Contratto c " +
           "LEFT JOIN FETCH c.rate " +
           "LEFT JOIN FETCH c.immobile " +
           "LEFT JOIN FETCH c.locatario " +
           "WHERE c.locatario.id = :locatarioId")
    List<Contratto> findByLocatarioId(Long locatarioId);

    /**
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a rate, immobile e locatario
     * Trova contratti per email utente (tramite locatario -> user)
     */
    @Query("SELECT DISTINCT c FROM Contratto c " +
           "LEFT JOIN FETCH c.rate " +
           "LEFT JOIN FETCH c.immobile " +
           "LEFT JOIN FETCH c.locatario " +
           "WHERE c.locatario.user.email = :email")
    List<Contratto> findByLocatarioUserEmail(String email);

    /**
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a rate, immobile e locatario
     */
    @Query("SELECT DISTINCT c FROM Contratto c " +
           "LEFT JOIN FETCH c.rate " +
           "LEFT JOIN FETCH c.immobile " +
           "LEFT JOIN FETCH c.locatario " +
           "WHERE c.immobile.id = :immobileId")
    List<Contratto> findByImmobileId(Long immobileId);
    
    /**
     * Query 3: Trova contratti con almeno 3 rate non pagate.
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a rate, immobile e locatario.
     * 
     * Utilizzato per identificare i contratti con problemi di morosità.
     * 
     * @return Lista di contratti che hanno almeno 3 rate con pagata = 'N'
     */
    @Query("SELECT DISTINCT c FROM Contratto c " +
           "LEFT JOIN FETCH c.rate " +
           "LEFT JOIN FETCH c.immobile " +
           "LEFT JOIN FETCH c.locatario " +
           "WHERE (SELECT COUNT(r) FROM Rata r WHERE r.contratto.id = c.id AND r.pagata = 'N') >= 3")
    List<Contratto> findContrattiConAlmenoTreRateNonPagate();
}
