package com.epicode.Progetto_Backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.epicode.Progetto_Backend.entity.Rata;

/**
 * RataRepository - Repository JPA per l'entità Rata.
 * 
 * Estende JpaRepository fornendo operazioni CRUD standard e metodi di query personalizzati.
 * 
 * Ottimizzazioni:
 * - Le query personalizzate utilizzano JOIN FETCH per caricare le relazioni in modo efficiente
 * - Carica sempre contratto, locatario e immobile per evitare lazy loading
 * 
 * Metodi disponibili:
 * - findByContrattoId: Trova tutte le rate di un contratto
 * - findByPagata: Trova rate per stato pagamento ('S' o 'N')
 * - findRateNonPagateByContratto: Trova rate non pagate di un contratto
 * - findRateScaduteNonPagate: Trova rate scadute e non pagate
 * - findByLocatarioId: Trova rate per ID locatario (tramite contratto)
 * - findByLocatarioUserEmail: Trova rate per email utente (tramite contratto -> locatario -> user)
 * 
 * @see com.epicode.Progetto_Backend.entity.Rata
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface RataRepository extends JpaRepository<Rata, Long> {
    
    /**
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a contratto e relazioni correlate
     */
    @Query("SELECT r FROM Rata r " +
           "LEFT JOIN FETCH r.contratto c " +
           "LEFT JOIN FETCH c.locatario " +
           "LEFT JOIN FETCH c.immobile " +
           "WHERE r.contratto.id = :contrattoId")
    List<Rata> findByContrattoId(Long contrattoId);
    
    /**
     * Trova rate per stato di pagamento.
     * 
     * @param pagata Stato pagamento: 'S' (pagata) o 'N' (non pagata)
     * @return Lista di rate con lo stato specificato
     */
    List<Rata> findByPagata(Character pagata);
    
    /**
     * Trova rate non pagate di un contratto specifico.
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a contratto e relazioni correlate.
     * 
     * @param contrattoId ID del contratto
     * @return Lista di rate non pagate del contratto
     */
    @Query("SELECT r FROM Rata r " +
           "LEFT JOIN FETCH r.contratto c " +
           "LEFT JOIN FETCH c.locatario " +
           "LEFT JOIN FETCH c.immobile " +
           "WHERE r.contratto.id = :contrattoId AND r.pagata = 'N'")
    List<Rata> findRateNonPagateByContratto(Long contrattoId);
    
    /**
     * Trova rate scadute e non pagate.
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a contratto e relazioni correlate.
     * 
     * Utilizzato per identificare situazioni di morosità.
     * 
     * @param data Data di riferimento (solitamente LocalDate.now())
     * @return Lista di rate con dataScadenza < data e pagata = 'N'
     */
    @Query("SELECT r FROM Rata r " +
           "LEFT JOIN FETCH r.contratto c " +
           "LEFT JOIN FETCH c.locatario " +
           "LEFT JOIN FETCH c.immobile " +
           "WHERE r.dataScadenza < :data AND r.pagata = 'N'")
    List<Rata> findRateScaduteNonPagate(LocalDate data);

    /**
     * Trova rate per ID locatario (tramite contratto).
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a contratto e relazioni correlate.
     * 
     * @param locatarioId ID del locatario
     * @return Lista di rate dei contratti del locatario
     */
    @Query("SELECT r FROM Rata r " +
           "LEFT JOIN FETCH r.contratto c " +
           "LEFT JOIN FETCH c.locatario " +
           "LEFT JOIN FETCH c.immobile " +
           "WHERE r.contratto.locatario.id = :locatarioId")
    List<Rata> findByLocatarioId(Long locatarioId);

    /**
     * Trova rate per email utente (tramite contratto -> locatario -> user).
     * 
     * Ottimizzato con JOIN FETCH per evitare N+1 quando si accede a contratto e relazioni correlate.
     * 
     * Utilizzato per permettere ai LOCATARIO di visualizzare le proprie rate.
     * 
     * @param email Email dell'utente
     * @return Lista di rate dei contratti del locatario associato all'utente
     */
    @Query("SELECT r FROM Rata r " +
           "LEFT JOIN FETCH r.contratto c " +
           "LEFT JOIN FETCH c.locatario l " +
           "LEFT JOIN FETCH l.user " +
           "LEFT JOIN FETCH c.immobile " +
           "WHERE r.contratto.locatario.user.email = :email")
    List<Rata> findByLocatarioUserEmail(String email);
}
