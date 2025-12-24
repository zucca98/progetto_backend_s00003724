package com.epicode.Progetto_Backend.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contratto - Entità per i contratti di affitto degli immobili.
 * 
 * Rappresenta un contratto di affitto tra un locatario e un immobile.
 * Alla creazione di un contratto, le rate vengono generate automaticamente
 * in base alla frequenza (MENSILE, TRIMESTRALE, etc.) e durata del contratto.
 * 
 * Calcolo rate automatico:
 * - Numero rate = (durataAnni × rate all'anno in base a frequenzaRata)
 * - Importo rata = canoneAnnuo / numero rate all'anno
 * 
 * Relazioni:
 * - Many-to-One con Locatario: Un contratto appartiene a un locatario
 * - Many-to-One con Immobile: Un contratto è per un immobile
 * - One-to-Many con Rata: Un contratto ha più rate
 * 
 * @see com.epicode.Progetto_Backend.entity.Locatario
 * @see com.epicode.Progetto_Backend.entity.Immobile
 * @see com.epicode.Progetto_Backend.entity.Rata
 * @see com.epicode.Progetto_Backend.entity.FrequenzaRata
 */
@Entity
@Table(name = "contratto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contratto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Locatario che affitta l'immobile */
    @ManyToOne
    @JoinColumn(name = "locatario_id", nullable = false)
    private Locatario locatario;
    
    /** Immobile oggetto del contratto */
    @ManyToOne
    @JoinColumn(name = "immobile_id", nullable = false)
    private Immobile immobile;
    
    /** Data di inizio del contratto */
    @Column(name = "data_inizio", nullable = false)
    private LocalDate dataInizio;
    
    /** Durata del contratto in anni */
    @Column(name = "durata_anni", nullable = false)
    private Integer durataAnni;
    
    /** Canone annuo di affitto in euro */
    @Column(name = "canone_annuo", nullable = false)
    private Double canoneAnnuo;
    
    /** Frequenza di pagamento delle rate (default: TRIMESTRALE) */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequenza_rata", nullable = false)
    @Builder.Default
    private FrequenzaRata frequenzaRata = FrequenzaRata.TRIMESTRALE;
    
    /** Lista delle rate del contratto (generate automaticamente, non serializzata in JSON) */
    @OneToMany(mappedBy = "contratto", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Rata> rate = new ArrayList<>();
}
