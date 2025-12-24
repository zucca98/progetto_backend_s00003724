package com.epicode.Progetto_Backend.dto;

import java.time.LocalDate;

import com.epicode.Progetto_Backend.entity.FrequenzaRata;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ContrattoRequestDTO - Data Transfer Object per la creazione/aggiornamento di contratti.
 * 
 * Utilizzato negli endpoint:
 * - POST /api/contratti (creazione)
 * - PUT /api/contratti/{id} (aggiornamento)
 * 
 * Alla creazione di un contratto, il sistema genera automaticamente tutte le rate
 * in base alla frequenza (MENSILE, TRIMESTRALE, etc.) e durata del contratto.
 * 
 * Esempio: Contratto di 3 anni con frequenza TRIMESTRALE genera 12 rate (4 per anno × 3 anni).
 * 
 * Validazioni:
 * - locatarioId, immobileId: Obbligatori (devono esistere nel database)
 * - dataInizio: Obbligatoria (formato LocalDate: YYYY-MM-DD)
 * - durataAnni: Obbligatoria e positiva
 * - canoneAnnuo: Obbligatorio e positivo
 * - frequenzaRata: Default TRIMESTRALE (MENSILE, BIMESTRALE, TRIMESTRALE, SEMESTRALE, ANNUALE)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContrattoRequestDTO {
    
    /** ID del locatario (deve esistere nel database) */
    @NotNull(message = "Locatario ID è obbligatorio")
    private Long locatarioId;
    
    /** ID dell'immobile (deve esistere nel database) */
    @NotNull(message = "Immobile ID è obbligatorio")
    private Long immobileId;
    
    /** Data di inizio del contratto (formato: YYYY-MM-DD) */
    @NotNull(message = "Data inizio è obbligatoria")
    private LocalDate dataInizio;
    
    /** Durata del contratto in anni */
    @NotNull(message = "Durata anni è obbligatoria")
    @Positive(message = "Durata anni deve essere positiva")
    private Integer durataAnni;
    
    /** Canone annuo di affitto in euro */
    @NotNull(message = "Canone annuo è obbligatorio")
    @Positive(message = "Canone annuo deve essere positivo")
    private Double canoneAnnuo;
    
    /** Frequenza di pagamento delle rate (default: TRIMESTRALE) */
    @Builder.Default
    private FrequenzaRata frequenzaRata = FrequenzaRata.TRIMESTRALE;
}
