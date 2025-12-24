package com.epicode.Progetto_Backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RataRequestDTO - Data Transfer Object per la creazione/aggiornamento di rate.
 * 
 * Utilizzato negli endpoint:
 * - POST /api/rate (creazione - raro, le rate vengono generate automaticamente)
 * - PUT /api/rate/{id} (aggiornamento)
 * 
 * Note:
 * - Le rate vengono generalmente generate automaticamente alla creazione di un contratto
 * - Il campo "pagata" utilizza un Character ('S' = pagata, 'N' = non pagata)
 * - Il numeroRata indica la posizione della rata nella sequenza del contratto
 * 
 * Validazioni:
 * - contrattoId: Obbligatorio (il contratto deve esistere)
 * - numeroRata: Obbligatorio e positivo
 * - dataScadenza: Obbligatoria (formato LocalDate: YYYY-MM-DD)
 * - importo: Obbligatorio e positivo (in euro)
 * - pagata: Default 'N', deve essere 'S' o 'N'
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RataRequestDTO {
    
    /** ID del contratto a cui appartiene la rata (deve esistere nel database) */
    @NotNull(message = "Contratto ID è obbligatorio")
    private Long contrattoId;
    
    /** Numero progressivo della rata nel contratto (es: 1, 2, 3...) */
    @NotNull(message = "Numero rata è obbligatorio")
    @Positive(message = "Numero rata deve essere positivo")
    private Integer numeroRata;
    
    /** Data di scadenza della rata (formato: YYYY-MM-DD) */
    @NotNull(message = "Data scadenza è obbligatoria")
    private LocalDate dataScadenza;
    
    /** Importo della rata in euro */
    @NotNull(message = "Importo è obbligatorio")
    @Positive(message = "Importo deve essere positivo")
    private Double importo;
    
    /** Stato pagamento: 'S' = pagata, 'N' = non pagata (default: 'N') */
    @Pattern(regexp = "[SN]", message = "Pagata deve essere 'S' o 'N'")
    @Builder.Default
    private Character pagata = 'N';
}
