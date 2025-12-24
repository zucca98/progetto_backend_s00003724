# Ottimizzazioni delle Query

Questo documento descrive le ottimizzazioni implementate per migliorare le performance delle query e prevenire problemi N+1.

## Indice

1. [Problema N+1](#problema-n1)
2. [Tecniche di Ottimizzazione](#tecniche-di-ottimizzazione)
3. [Ottimizzazioni Implementate](#ottimizzazioni-implementate)
4. [Test di Performance](#test-di-performance)
5. [Best Practices](#best-practices)

## Problema N+1

Il problema N+1 si verifica quando:
- Si esegue 1 query per recuperare una lista di entità (es. contratti)
- Per ogni entità, si eseguono N query aggiuntive per caricare le relazioni lazy (es. rate, immobile, locatario)
- Risultato: 1 + N query invece di una singola query ottimizzata

### Esempio di Problema N+1 (NON OTTIMIZZATO)

```java
// Query 1: Recupera tutti i contratti
List<Contratto> contratti = contrattoRepository.findAll();

// Query 2-N+1: Per ogni contratto, carica le rate (lazy loading)
for (Contratto c : contratti) {
    List<Rata> rate = c.getRate(); // Query aggiuntiva per ogni contratto!
}
```

**Risultato**: Se ci sono 10 contratti, vengono eseguite 11 query (1 + 10).

## Tecniche di Ottimizzazione

### 1. @EntityGraph

L'annotazione `@EntityGraph` di Spring Data JPA permette di specificare quali relazioni caricare eager in una singola query.

**Vantaggi**:
- Facile da usare
- Integrato con Spring Data JPA
- Supporta paginazione

**Esempio**:
```java
@EntityGraph(attributePaths = {"rate", "immobile", "locatario"})
Page<Contratto> findAll(Pageable pageable);
```

### 2. JOIN FETCH

La clausola `JOIN FETCH` in JPQL/HQL permette di caricare le relazioni in una singola query SQL.

**Vantaggi**:
- Controllo completo sulla query
- Supporta condizioni complesse
- Efficace per query personalizzate

**Esempio**:
```java
@Query("SELECT DISTINCT c FROM Contratto c " +
       "LEFT JOIN FETCH c.rate " +
       "LEFT JOIN FETCH c.immobile " +
       "LEFT JOIN FETCH c.locatario " +
       "WHERE c.locatario.id = :locatarioId")
List<Contratto> findByLocatarioId(Long locatarioId);
```

**Nota**: `DISTINCT` è necessario quando si usa `JOIN FETCH` con `List` per evitare duplicati.

## Ottimizzazioni Implementate

### ContrattoRepository

#### 1. findAll() con @EntityGraph
```java
@EntityGraph(attributePaths = {"rate", "immobile", "locatario"})
Page<Contratto> findAll(Pageable pageable);
```
**Ottimizzazione**: Carica rate, immobile e locatario in una singola query.

#### 2. findById() con @EntityGraph
```java
@EntityGraph(attributePaths = {"rate", "immobile", "locatario"})
Optional<Contratto> findById(Long id);
```
**Ottimizzazione**: Carica tutte le relazioni necessarie in una query.

#### 3. findByLocatarioId() con JOIN FETCH
```java
@Query("SELECT DISTINCT c FROM Contratto c " +
       "LEFT JOIN FETCH c.rate " +
       "LEFT JOIN FETCH c.immobile " +
       "LEFT JOIN FETCH c.locatario " +
       "WHERE c.locatario.id = :locatarioId")
List<Contratto> findByLocatarioId(Long locatarioId);
```
**Ottimizzazione**: Una singola query con tutti i JOIN necessari.

#### 4. findByLocatarioUserEmail() con JOIN FETCH
```java
@Query("SELECT DISTINCT c FROM Contratto c " +
       "LEFT JOIN FETCH c.rate " +
       "LEFT JOIN FETCH c.immobile " +
       "LEFT JOIN FETCH c.locatario " +
       "WHERE c.locatario.user.email = :email")
List<Contratto> findByLocatarioUserEmail(String email);
```
**Ottimizzazione**: Include anche la relazione user tramite locatario.

#### 5. findByImmobileId() con JOIN FETCH
```java
@Query("SELECT DISTINCT c FROM Contratto c " +
       "LEFT JOIN FETCH c.rate " +
       "LEFT JOIN FETCH c.immobile " +
       "LEFT JOIN FETCH c.locatario " +
       "WHERE c.immobile.id = :immobileId")
List<Contratto> findByImmobileId(Long immobileId);
```
**Ottimizzazione**: Carica tutti i contratti di un immobile con le relazioni.

#### 6. findContrattiConAlmenoTreRateNonPagate() con JOIN FETCH
```java
@Query("SELECT DISTINCT c FROM Contratto c " +
       "LEFT JOIN FETCH c.rate " +
       "LEFT JOIN FETCH c.immobile " +
       "LEFT JOIN FETCH c.locatario " +
       "WHERE (SELECT COUNT(r) FROM Rata r WHERE r.contratto.id = c.id AND r.pagata = 'N') >= 3")
List<Contratto> findContrattiConAlmenoTreRateNonPagate();
```
**Ottimizzazione**: Query complessa con subquery, ma mantiene l'ottimizzazione per le relazioni.

### ImmobileRepository

#### 1. findAll() con @EntityGraph
```java
@EntityGraph(attributePaths = {"contratti", "manutenzioni"})
Page<Immobile> findAll(Pageable pageable);
```
**Ottimizzazione**: Carica contratti e manutenzioni in una singola query.

#### 2. findById() con @EntityGraph
```java
@EntityGraph(attributePaths = {"contratti", "manutenzioni"})
Optional<Immobile> findById(Long id);
```
**Ottimizzazione**: Carica tutte le relazioni necessarie.

#### 3. findByCitta() con @EntityGraph
```java
@EntityGraph(attributePaths = {"contratti", "manutenzioni"})
List<Immobile> findByCitta(String citta);
```
**Ottimizzazione**: Mantiene l'ottimizzazione anche per query con filtri.

#### 4. findByTipo() con @EntityGraph
```java
@EntityGraph(attributePaths = {"contratti", "manutenzioni"})
List<Immobile> findByTipo(TipoImmobile tipo);
```
**Ottimizzazione**: Applicata anche alle query per tipo.

### LocatarioRepository

#### 1. findAll() con @EntityGraph
```java
@EntityGraph(attributePaths = {"contratti", "manutenzioni", "user"})
Page<Locatario> findAll(Pageable pageable);
```
**Ottimizzazione**: Carica contratti, manutenzioni e user in una singola query.

#### 2. findById() con @EntityGraph
```java
@EntityGraph(attributePaths = {"contratti", "manutenzioni", "user"})
Optional<Locatario> findById(Long id);
```
**Ottimizzazione**: Include tutte le relazioni principali.

#### 3. findByCf() con @EntityGraph
```java
@EntityGraph(attributePaths = {"contratti", "manutenzioni", "user"})
Optional<Locatario> findByCf(String cf);
```
**Ottimizzazione**: Mantiene l'ottimizzazione per query di ricerca.

#### 4. findByUserId() con @EntityGraph
```java
@EntityGraph(attributePaths = {"contratti", "manutenzioni", "user"})
Optional<Locatario> findByUserId(Long userId);
```
**Ottimizzazione**: Include tutte le relazioni anche per query tramite user.

#### 5. findLocatariConContrattiLunghiDurata() con JOIN FETCH
```java
@Query("SELECT DISTINCT l FROM Locatario l " +
       "LEFT JOIN FETCH l.contratti " +
       "LEFT JOIN FETCH l.manutenzioni " +
       "LEFT JOIN FETCH l.user " +
       "JOIN l.contratti c WHERE c.durataAnni > 2")
List<Locatario> findLocatariConContrattiLunghiDurata();
```
**Ottimizzazione**: Query complessa con filtro, ma mantiene l'ottimizzazione.

### RataRepository

#### 1. findByContrattoId() con JOIN FETCH
```java
@Query("SELECT r FROM Rata r " +
       "LEFT JOIN FETCH r.contratto c " +
       "LEFT JOIN FETCH c.locatario " +
       "LEFT JOIN FETCH c.immobile " +
       "WHERE r.contratto.id = :contrattoId")
List<Rata> findByContrattoId(Long contrattoId);
```
**Ottimizzazione**: Carica contratto e tutte le sue relazioni in una query.

#### 2. findRateNonPagateByContratto() con JOIN FETCH
```java
@Query("SELECT r FROM Rata r " +
       "LEFT JOIN FETCH r.contratto c " +
       "LEFT JOIN FETCH c.locatario " +
       "LEFT JOIN FETCH c.immobile " +
       "WHERE r.contratto.id = :contrattoId AND r.pagata = 'N'")
List<Rata> findRateNonPagateByContratto(Long contrattoId);
```
**Ottimizzazione**: Include filtro per rate non pagate, mantenendo l'ottimizzazione.

#### 3. findRateScaduteNonPagate() con JOIN FETCH
```java
@Query("SELECT r FROM Rata r " +
       "LEFT JOIN FETCH r.contratto c " +
       "LEFT JOIN FETCH c.locatario " +
       "LEFT JOIN FETCH c.immobile " +
       "WHERE r.dataScadenza < :data AND r.pagata = 'N'")
List<Rata> findRateScaduteNonPagate(LocalDate data);
```
**Ottimizzazione**: Query con filtro temporale, mantenendo l'ottimizzazione.

#### 4. findByLocatarioId() con JOIN FETCH
```java
@Query("SELECT r FROM Rata r " +
       "LEFT JOIN FETCH r.contratto c " +
       "LEFT JOIN FETCH c.locatario " +
       "LEFT JOIN FETCH c.immobile " +
       "WHERE r.contratto.locatario.id = :locatarioId")
List<Rata> findByLocatarioId(Long locatarioId);
```
**Ottimizzazione**: Carica tutte le rate di un locatario con le relazioni.

#### 5. findByLocatarioUserEmail() con JOIN FETCH
```java
@Query("SELECT r FROM Rata r " +
       "LEFT JOIN FETCH r.contratto c " +
       "LEFT JOIN FETCH c.locatario l " +
       "LEFT JOIN FETCH l.user " +
       "LEFT JOIN FETCH c.immobile " +
       "WHERE r.contratto.locatario.user.email = :email")
List<Rata> findByLocatarioUserEmail(String email);
```
**Ottimizzazione**: Include anche la relazione user tramite locatario.

### ManutenzioneRepository

#### 1. findByImmobileId() con JOIN FETCH
```java
@Query("SELECT m FROM Manutenzione m " +
       "LEFT JOIN FETCH m.immobile " +
       "LEFT JOIN FETCH m.locatario l " +
       "LEFT JOIN FETCH l.user " +
       "WHERE m.immobile.id = :immobileId")
List<Manutenzione> findByImmobileId(Long immobileId);
```
**Ottimizzazione**: Carica immobile, locatario e user in una query.

#### 2. findByLocatarioId() con JOIN FETCH
```java
@Query("SELECT m FROM Manutenzione m " +
       "LEFT JOIN FETCH m.immobile " +
       "LEFT JOIN FETCH m.locatario l " +
       "LEFT JOIN FETCH l.user " +
       "WHERE m.locatario.id = :locatarioId")
List<Manutenzione> findByLocatarioId(Long locatarioId);
```
**Ottimizzazione**: Include tutte le relazioni necessarie.

#### 3. findManutenzioniByLocatarioAndAnno() con JOIN FETCH
```java
@Query("SELECT m FROM Manutenzione m " +
       "LEFT JOIN FETCH m.immobile " +
       "LEFT JOIN FETCH m.locatario l " +
       "LEFT JOIN FETCH l.user " +
       "WHERE m.locatario.id = :locatarioId AND YEAR(m.dataMan) = :anno")
List<Manutenzione> findManutenzioniByLocatarioAndAnno(Long locatarioId, int anno);
```
**Ottimizzazione**: Query con filtro per anno, mantenendo l'ottimizzazione.

#### 4. findByLocatarioUserEmail() con JOIN FETCH
```java
@Query("SELECT m FROM Manutenzione m " +
       "LEFT JOIN FETCH m.immobile " +
       "LEFT JOIN FETCH m.locatario l " +
       "LEFT JOIN FETCH l.user " +
       "WHERE m.locatario.user.email = :email")
List<Manutenzione> findByLocatarioUserEmail(String email);
```
**Ottimizzazione**: Include la relazione user tramite locatario.

## Test di Performance

Sono stati implementati test di performance per verificare che le ottimizzazioni funzionino correttamente.

### File: QueryPerformanceTest.java

I test verificano che:
1. Il numero di query SQL eseguite sia minimo
2. Non si verifichino problemi N+1
3. Le relazioni vengano caricate in una singola query

#### Esempi di Test

**testContrattoFindAllWithEntityGraph()**
- Verifica che `findAll()` esegua al massimo 2 query (1 per i dati + eventualmente 1 per il count)
- Accede a tutte le relazioni (rate, immobile, locatario, user)
- Verifica che non ci siano query aggiuntive per ogni relazione

**testContrattoFindByIdWithEntityGraph()**
- Verifica che `findById()` esegua esattamente 1 query
- Carica tutte le relazioni necessarie

**testContrattoFindByLocatarioIdWithJoinFetch()**
- Verifica che `findByLocatarioId()` esegua esattamente 1 query
- Usa JOIN FETCH per caricare tutte le relazioni

### Come Eseguire i Test

```bash
# Esegui tutti i test di performance
mvn test -Dtest=QueryPerformanceTest

# Esegui un test specifico
mvn test -Dtest=QueryPerformanceTest#testContrattoFindAllWithEntityGraph
```

### Metriche Verificate

I test utilizzano Hibernate Statistics per contare le query eseguite:
- `statistics.getQueryExecutionCount()`: Numero totale di query eseguite
- Verifica che il numero sia minimo (1-2 query invece di N+1)

## Best Practices

### Quando Usare @EntityGraph

✅ **Usa @EntityGraph quando**:
- Hai bisogno di paginazione
- Le relazioni sono sempre necessarie
- Vuoi semplicità e leggibilità

### Quando Usare JOIN FETCH

✅ **Usa JOIN FETCH quando**:
- Hai bisogno di condizioni complesse nella query
- Vuoi controllo completo sulla query SQL generata
- Le relazioni sono opzionali (LEFT JOIN FETCH)

### Quando NON Ottimizzare

❌ **NON ottimizzare quando**:
- Le relazioni non sono sempre necessarie
- Il caricamento eager causerebbe problemi di memoria
- Le query restituiscono solo aggregazioni (COUNT, SUM, etc.)

### Regole Generali

1. **Usa DISTINCT con JOIN FETCH e List**: Evita duplicati quando una relazione ha multiple righe
2. **LEFT JOIN FETCH per relazioni opzionali**: Usa LEFT JOIN invece di JOIN per relazioni nullable
3. **Testa le performance**: Verifica sempre che le ottimizzazioni funzionino con test
4. **Monitora le query**: Usa logging SQL per verificare le query generate

### Configurazione Logging SQL

Per vedere le query eseguite durante lo sviluppo, aggiungi in `application.properties`:

```properties
# Logging SQL queries
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Impatto delle Ottimizzazioni

### Prima delle Ottimizzazioni

**Esempio**: Recuperare 10 contratti con le loro rate
- Query 1: SELECT * FROM contratti (1 query)
- Query 2-11: SELECT * FROM rate WHERE contratto_id = ? (10 query)
- **Totale**: 11 query

### Dopo le Ottimizzazioni

**Esempio**: Recuperare 10 contratti con le loro rate
- Query 1: SELECT c.*, r.* FROM contratti c LEFT JOIN rate r ON c.id = r.contratto_id (1 query)
- **Totale**: 1 query

### Benefici

1. **Performance**: Riduzione drastica del numero di query (da N+1 a 1)
2. **Latenza**: Riduzione del tempo di risposta (meno round-trip al database)
3. **Scalabilità**: Miglior gestione del carico sul database
4. **Costi**: Riduzione del carico su database cloud (meno query = meno costi)

## Conclusioni

Le ottimizzazioni implementate garantiscono:
- ✅ Nessun problema N+1
- ✅ Performance ottimali
- ✅ Scalabilità migliorata
- ✅ Codice manutenibile e ben documentato

Tutti i repository principali sono stati ottimizzati e testati per garantire le migliori performance possibili.

