package com.epicode.Progetto_Backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.epicode.Progetto_Backend.entity.Manutenzione;

/**
 * ManutenzioneRepository - Repository JPA per l'entità Manutenzione.
 * 
 * Estende JpaRepository fornendo operazioni CRUD standard e metodi di query personalizzati.
 * 
 * Ottimizzazioni:
 * - Le query personalizzate utilizzano JOIN FETCH per caricare le relazioni in modo efficiente
 * - Carica sempre immobile, locatario e user per evitare lazy loading
 * 
 * Metodi disponibili:
 * - findByImmobileId: Trova manutenzioni per ID immobile
 * - findByLocatarioId: Trova manutenzioni per ID locatario
 * - findManutenzioniByLocatarioAndAnno: Query personalizzata per manutenzioni di un locatario in un anno
 * - findDateManutenzioniByLocatarioAndImportoMaggiore: Query per date manutenzioni con importo > X
 * - findTotaleSpeseManutenzionePerAnnoCitta: Query aggregata per statistiche spese
 * - findByLocatarioUserEmail: Trova manutenzioni per email utente (tramite locatario)
 * 
 * Query personalizzate:
 * - Query 4: Manutenzioni di un locatario in un determinato anno
 * - Query 5: Date manutenzioni con importo maggiore di un valore
 * - Query 6: Totale spese manutenzione per anno e città
 * 
 * @see com.epicode.Progetto_Backend.entity.Manutenzione
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface ManutenzioneRepository extends JpaRepository<Manutenzione, Long> {
    
    /**
     * Trova manutenzioni per ID immobile.
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a immobile e locatario.
     * 
     * @param immobileId ID dell'immobile
     * @return Lista di manutenzioni effettuate sull'immobile
     */
    @Query("SELECT m FROM Manutenzione m " +
           "LEFT JOIN FETCH m.immobile " +
           "LEFT JOIN FETCH m.locatario l " +
           "LEFT JOIN FETCH l.user " +
           "WHERE m.immobile.id = :immobileId")
    List<Manutenzione> findByImmobileId(Long immobileId);
    
    /**
     * Trova manutenzioni per ID locatario.
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a immobile e locatario.
     * 
     * @param locatarioId ID del locatario
     * @return Lista di manutenzioni associate al locatario
     */
    @Query("SELECT m FROM Manutenzione m " +
           "LEFT JOIN FETCH m.immobile " +
           "LEFT JOIN FETCH m.locatario l " +
           "LEFT JOIN FETCH l.user " +
           "WHERE m.locatario.id = :locatarioId")
    List<Manutenzione> findByLocatarioId(Long locatarioId);
    
    /**
     * Query 4: Trova manutenzioni di un locatario in un determinato anno.
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a immobile e locatario.
     * 
     * @param locatarioId ID del locatario
     * @param anno Anno delle manutenzioni (es: 2024)
     * @return Lista di manutenzioni del locatario per l'anno specificato
     */
    @Query("SELECT m FROM Manutenzione m " +
           "LEFT JOIN FETCH m.immobile " +
           "LEFT JOIN FETCH m.locatario l " +
           "LEFT JOIN FETCH l.user " +
           "WHERE m.locatario.id = :locatarioId AND YEAR(m.dataMan) = :anno")
    List<Manutenzione> findManutenzioniByLocatarioAndAnno(@Param("locatarioId") Long locatarioId, @Param("anno") int anno);
    
    /**
     * Query 5: Trova le date in cui un locatario ha eseguito manutenzioni con importo maggiore di un valore.
     * 
     * Non necessita JOIN FETCH perché restituisce solo date (LocalDate).
     * 
     * Utile per identificare le manutenzioni più costose di un locatario.
     * 
     * @param locatarioId ID del locatario
     * @param importoMinimo Importo minimo in euro
     * @return Lista di date (LocalDate) ordinate delle manutenzioni che superano l'importo
     */
    @Query("SELECT m.dataMan FROM Manutenzione m WHERE m.locatario.id = :locatarioId AND m.importo > :importoMinimo ORDER BY m.dataMan")
    List<LocalDate> findDateManutenzioniByLocatarioAndImportoMaggiore(@Param("locatarioId") Long locatarioId, @Param("importoMinimo") Double importoMinimo);
    
    /**
     * Query 6: Calcola il totale delle spese di manutenzione per anno e città.
     * 
     * Non necessita JOIN FETCH perché restituisce solo aggregazioni.
     * 
     * Restituisce una lista di array Object[] dove:
     * - Object[0]: Anno (Integer)
     * - Object[1]: Città (String)
     * - Object[2]: Totale spese (Double)
     * 
     * @return Lista di array [anno, città, totale] ordinata per anno e città
     */
    @Query("SELECT YEAR(m.dataMan), m.immobile.citta, SUM(m.importo) " +
           "FROM Manutenzione m " +
           "GROUP BY YEAR(m.dataMan), m.immobile.citta " +
           "ORDER BY YEAR(m.dataMan), m.immobile.citta")
    List<Object[]> findTotaleSpeseManutenzionePerAnnoCitta();

    /**
     * Trova manutenzioni per email utente (tramite locatario -> user).
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a immobile e locatario.
     * 
     * Utilizzato per permettere ai LOCATARIO di visualizzare le proprie manutenzioni.
     * 
     * @param email Email dell'utente
     * @return Lista di manutenzioni del locatario associato all'utente
     */
    @Query("SELECT m FROM Manutenzione m " +
           "LEFT JOIN FETCH m.immobile " +
           "LEFT JOIN FETCH m.locatario l " +
           "LEFT JOIN FETCH l.user " +
           "WHERE m.locatario.user.email = :email")
    List<Manutenzione> findByLocatarioUserEmail(String email);
}
