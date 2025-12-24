package com.epicode.Progetto_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PageResponse - Data Transfer Object generico per risposte paginate.
 * 
 * Wrapper generico per incapsulare dati paginati restituiti dagli endpoint
 * che supportano la paginazione (es: GET /api/immobili, GET /api/contratti).
 * 
 * Questo DTO segue lo standard HATEOAS e Spring Data pagination per fornire
 * informazioni complete sulla paginazione, permettendo al client di navigare
 * tra le pagine e conoscere il totale degli elementi.
 * 
 * Utilizzo:
 * - T: Tipo generico del contenuto della pagina (es: Immobile, Contratto, User)
 * 
 * Esempio di risposta JSON:
 * {
 *   "content": [...],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 150,
 *   "totalPages": 8,
 *   "first": true,
 *   "last": false
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    /** Lista degli elementi nella pagina corrente */
    private List<T> content;
    
    /** Numero della pagina corrente (0-based) */
    private int page;
    
    /** Dimensione della pagina (numero di elementi per pagina) */
    private int size;
    
    /** Numero totale di elementi in tutte le pagine */
    private long totalElements;
    
    /** Numero totale di pagine disponibili */
    private int totalPages;
    
    /** true se questa è la prima pagina */
    private boolean first;
    
    /** true se questa è l'ultima pagina */
    private boolean last;
}

