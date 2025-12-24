package com.epicode.Progetto_Backend.dto;

import com.epicode.Progetto_Backend.entity.TipoImmobile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ImmobileRequestDTO - Data Transfer Object per la creazione/aggiornamento di immobili.
 * 
 * Utilizzato negli endpoint:
 * - POST /api/immobili (creazione)
 * - PUT /api/immobili/{id} (aggiornamento)
 * 
 * Il DTO supporta tre tipi di immobili tramite ereditarietà JPA (InheritanceType.JOINED):
 * - APPARTAMENTO: Richiede piano e numCamere
 * - NEGOZIO: Richiede vetrine e magazzinoMq
 * - UFFICIO: Richiede postiLavoro e saleRiunioni
 * 
 * I campi specifici per tipo sono opzionali nel DTO, ma devono essere forniti
 * in base al tipo selezionato per creare correttamente l'entità specifica.
 * 
 * Validazioni:
 * - indirizzo, citta: Obbligatori e non vuoti
 * - superficie: Obbligatoria e positiva
 * - tipo: Obbligatorio (APPARTAMENTO, NEGOZIO, UFFICIO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImmobileRequestDTO {
    
    /** Indirizzo completo dell'immobile */
    @NotBlank(message = "Indirizzo è obbligatorio")
    private String indirizzo;
    
    /** Città dell'immobile */
    @NotBlank(message = "Città è obbligatoria")
    private String citta;
    
    /** Superficie totale in metri quadri */
    @NotNull(message = "Superficie è obbligatoria")
    @Positive(message = "Superficie deve essere positiva")
    private Double superficie;
    
    /** Tipo di immobile (APPARTAMENTO, NEGOZIO, UFFICIO) */
    @NotNull(message = "Tipo è obbligatorio")
    private TipoImmobile tipo;
    
    // Campi per Appartamento
    /** Piano dell'appartamento (richiesto se tipo = APPARTAMENTO) */
    private Integer piano;
    
    /** Numero di camere dell'appartamento (richiesto se tipo = APPARTAMENTO) */
    private Integer numCamere;
    
    // Campi per Negozio
    /** Numero di vetrine del negozio (richiesto se tipo = NEGOZIO) */
    private Integer vetrine;
    
    /** Metri quadri del magazzino (richiesto se tipo = NEGOZIO) */
    private Double magazzinoMq;
    
    // Campi per Ufficio
    /** Numero di posti di lavoro (richiesto se tipo = UFFICIO) */
    private Integer postiLavoro;
    
    /** Numero di sale riunioni (richiesto se tipo = UFFICIO) */
    private Integer saleRiunioni;
}
