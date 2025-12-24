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

import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.TipoImmobile;

/**
 * ImmobileRepository - Repository JPA per l'entità Immobile.
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
 * Query personalizzate:
 * - countImmobiliAffittatiPerCitta: Statistiche immobili affittati per città
 * - countImmobiliPerTipo: Conteggio immobili per tipo (APPARTAMENTO, NEGOZIO, UFFICIO)
 * 
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see org.springframework.data.jpa.repository.EntityGraph
 */
@Repository
public interface ImmobileRepository extends JpaRepository<Immobile, Long> {
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a contratti.
     * Nota: manutenzioni non è inclusa per evitare MultipleBagFetchException (Hibernate non può
     * fare fetch simultaneo di due collezioni List). Le manutenzioni possono essere caricate
     * separatamente se necessario.
     */
    @EntityGraph(attributePaths = {"contratti"})
    @Override
    @NonNull
    Page<Immobile> findAll(@NonNull Pageable pageable);
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a contratti.
     * Nota: manutenzioni non è inclusa per evitare MultipleBagFetchException.
     */
    @EntityGraph(attributePaths = {"contratti"})
    @Override
    @NonNull
    Optional<Immobile> findById(@NonNull Long id);
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a contratti.
     * Nota: manutenzioni non è inclusa per evitare MultipleBagFetchException.
     */
    @EntityGraph(attributePaths = {"contratti"})
    List<Immobile> findByCitta(String citta);
    
    /**
     * Ottimizzato con @EntityGraph per evitare N+1 quando si accede a contratti.
     * Nota: manutenzioni non è inclusa per evitare MultipleBagFetchException.
     */
    @EntityGraph(attributePaths = {"contratti"})
    List<Immobile> findByTipo(TipoImmobile tipo);
    
    /**
     * Query 2: Numero di immobili affittati per ogni città.
     * 
     * Restituisce una lista di array Object[] dove:
     * - Object[0]: Nome della città (String)
     * - Object[1]: Numero di immobili affittati (Long)
     * 
     * Solo gli immobili con almeno un contratto vengono conteggiati.
     * 
     * @return Lista di array [città, conteggio] ordinata per città
     */
    @Query("SELECT i.citta, COUNT(DISTINCT i) FROM Immobile i JOIN i.contratti c GROUP BY i.citta")
    List<Object[]> countImmobiliAffittatiPerCitta();
    
    /**
     * Query 7: Conteggio immobili per tipo.
     * 
     * Restituisce una lista di array Object[] dove:
     * - Object[0]: Tipo immobile (TipoImmobile enum)
     * - Object[1]: Conteggio totale (Long)
     * 
     * @return Lista di array [tipo, conteggio] per ogni tipo di immobile
     */
    @Query("SELECT i.tipo, COUNT(i) FROM Immobile i GROUP BY i.tipo")
    List<Object[]> countImmobiliPerTipo();
}
