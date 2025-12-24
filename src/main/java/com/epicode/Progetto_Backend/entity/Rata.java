package com.epicode.Progetto_Backend.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rata - Entità per le rate di pagamento di un contratto.
 * 
 * Rappresenta una singola rata di affitto associata a un contratto.
 * Le rate vengono generate automaticamente alla creazione di un contratto
 * in base alla frequenza (MENSILE, TRIMESTRALE, etc.) e durata.
 * 
 * Lo stato di pagamento utilizza un Character: 'S' (pagata) o 'N' (non pagata).
 * Il numeroRata indica la posizione della rata nella sequenza del contratto (1, 2, 3...).
 * 
 * Relazioni:
 * - Many-to-One con Contratto: Una rata appartiene a un contratto
 * 
 * @see com.epicode.Progetto_Backend.entity.Contratto
 */
@Entity
@Table(name = "rata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Contratto a cui appartiene questa rata */
    @ManyToOne
    @JoinColumn(name = "contratto_id", nullable = false)
    private Contratto contratto;
    
    /** Numero progressivo della rata nel contratto (es: 1, 2, 3...) */
    @Column(name = "numero_rata", nullable = false)
    private Integer numeroRata;
    
    /** Data di scadenza della rata */
    @Column(name = "data_scadenza", nullable = false)
    private LocalDate dataScadenza;
    
    /** Importo della rata in euro */
    @Column(nullable = false)
    private Double importo;
    
    /** Stato pagamento: 'S' = pagata, 'N' = non pagata (default: 'N') */
    @Column(nullable = false, length = 1)
    @Builder.Default
    private Character pagata = 'N'; // 'S' o 'N'
}
