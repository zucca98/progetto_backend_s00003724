package com.epicode.Progetto_Backend.graphql;

import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.epicode.Progetto_Backend.entity.Appartamento;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Negozio;
import com.epicode.Progetto_Backend.entity.Rata;
import com.epicode.Progetto_Backend.entity.Ufficio;

/**
 * FieldResolver - Risolutore GraphQL per campi specifici e conversioni di tipo.
 * 
 * Gestisce la risoluzione di campi GraphQL che richiedono logica specifica
 * o conversioni di tipo non gestite automaticamente da Spring GraphQL.
 * 
 * Funzionalità principali:
 * 1. Risoluzione dei campi specifici per i sottotipi di Immobile (appartamento, negozio, ufficio)
 *    - Permette di queryare i campi specifici quando si ha un Immobile polimorfico
 * 2. Conversione del campo pagata da Character ('S'/'N') a Boolean per GraphQL
 * 3. Risoluzione esplicita dei campi ereditati per Appartamento, Negozio, Ufficio
 * 
 * I metodi annotati con @SchemaMapping vengono chiamati da Spring GraphQL
 * quando è necessario risolvere un campo specifico per un tipo.
 * 
 * @see org.springframework.graphql.data.method.annotation.SchemaMapping
 */
@Controller
public class FieldResolver {

    /**
     * Risolve il campo "appartamento" per un Immobile.
     * 
     * Se l'immobile è un'istanza di Appartamento, restituisce l'oggetto castato,
     * altrimenti null. Permette di queryare i campi specifici (piano, numCamere)
     * quando si ha un Immobile polimorfico.
     * 
     * @param immobile Immobile da verificare
     * @return Appartamento se l'immobile è un Appartamento, null altrimenti
     */
    @SchemaMapping(typeName = "Immobile", field = "appartamento")
    public Appartamento appartamento(Immobile immobile) {
        if (immobile instanceof Appartamento appartamento) {
            return appartamento;
        }
        return null;
    }

    /**
     * Risolve il campo "ufficio" per un Immobile.
     * 
     * Se l'immobile è un'istanza di Ufficio, restituisce l'oggetto castato,
     * altrimenti null. Permette di queryare i campi specifici (postiLavoro, saleRiunioni)
     * quando si ha un Immobile polimorfico.
     * 
     * @param immobile Immobile da verificare
     * @return Ufficio se l'immobile è un Ufficio, null altrimenti
     */
    @SchemaMapping(typeName = "Immobile", field = "ufficio")
    public Ufficio ufficio(Immobile immobile) {
        if (immobile instanceof Ufficio ufficio) {
            return ufficio;
        }
        return null;
    }

    /**
     * Risolve il campo "negozio" per un Immobile.
     * 
     * Se l'immobile è un'istanza di Negozio, restituisce l'oggetto castato,
     * altrimenti null. Permette di queryare i campi specifici (vetrine, magazzinoMq)
     * quando si ha un Immobile polimorfico.
     * 
     * @param immobile Immobile da verificare
     * @return Negozio se l'immobile è un Negozio, null altrimenti
     */
    @SchemaMapping(typeName = "Immobile", field = "negozio")
    public Negozio negozio(Immobile immobile) {
        if (immobile instanceof Negozio negozio) {
            return negozio;
        }
        return null;
    }

    /**
     * Risolve il campo "pagata" per una Rata convertendo Character a Boolean.
     * 
     * Converte il campo pagata da Character ('S' = true, 'N' = false) a Boolean
     * per compatibilità con il tipo Boolean richiesto dallo schema GraphQL.
     * 
     * @param rata Rata di cui verificare lo stato di pagamento
     * @return true se pagata = 'S', false altrimenti
     */
    @SchemaMapping(typeName = "Rata", field = "pagata")
    public Boolean pagata(Rata rata) {
        return rata.getPagata() != null && rata.getPagata() == 'S';
    }

    // ==================== Appartamento Field Resolvers ====================
    // Risolvono esplicitamente i campi ereditati da Immobile per Appartamento
    // (necessario per l'ereditarietà JPA JOINED con GraphQL)
    
    @SchemaMapping(typeName = "Appartamento", field = "id")
    public Long appartamentoId(Appartamento appartamento) {
        return appartamento.getId();
    }

    @SchemaMapping(typeName = "Appartamento", field = "indirizzo")
    public String appartamentoIndirizzo(Appartamento appartamento) {
        return appartamento.getIndirizzo();
    }

    @SchemaMapping(typeName = "Appartamento", field = "citta")
    public String appartamentoCitta(Appartamento appartamento) {
        return appartamento.getCitta();
    }

    @SchemaMapping(typeName = "Appartamento", field = "superficie")
    public Double appartamentoSuperficie(Appartamento appartamento) {
        return appartamento.getSuperficie();
    }

    @SchemaMapping(typeName = "Appartamento", field = "tipo")
    public com.epicode.Progetto_Backend.entity.TipoImmobile appartamentoTipo(Appartamento appartamento) {
        return appartamento.getTipo();
    }

    // ==================== Ufficio Field Resolvers ====================
    // Risolvono esplicitamente i campi ereditati da Immobile per Ufficio
    
    @SchemaMapping(typeName = "Ufficio", field = "id")
    public Long ufficioId(Ufficio ufficio) {
        return ufficio.getId();
    }

    @SchemaMapping(typeName = "Ufficio", field = "indirizzo")
    public String ufficioIndirizzo(Ufficio ufficio) {
        return ufficio.getIndirizzo();
    }

    @SchemaMapping(typeName = "Ufficio", field = "citta")
    public String ufficioCitta(Ufficio ufficio) {
        return ufficio.getCitta();
    }

    @SchemaMapping(typeName = "Ufficio", field = "superficie")
    public Double ufficioSuperficie(Ufficio ufficio) {
        return ufficio.getSuperficie();
    }

    @SchemaMapping(typeName = "Ufficio", field = "tipo")
    public com.epicode.Progetto_Backend.entity.TipoImmobile ufficioTipo(Ufficio ufficio) {
        return ufficio.getTipo();
    }

    // ==================== Negozio Field Resolvers ====================
    // Risolvono esplicitamente i campi ereditati da Immobile per Negozio
    
    @SchemaMapping(typeName = "Negozio", field = "id")
    public Long negozioId(Negozio negozio) {
        return negozio.getId();
    }

    @SchemaMapping(typeName = "Negozio", field = "indirizzo")
    public String negozioIndirizzo(Negozio negozio) {
        return negozio.getIndirizzo();
    }

    @SchemaMapping(typeName = "Negozio", field = "citta")
    public String negozioCitta(Negozio negozio) {
        return negozio.getCitta();
    }

    @SchemaMapping(typeName = "Negozio", field = "superficie")
    public Double negozioSuperficie(Negozio negozio) {
        return negozio.getSuperficie();
    }

    @SchemaMapping(typeName = "Negozio", field = "tipo")
    public com.epicode.Progetto_Backend.entity.TipoImmobile negozioTipo(Negozio negozio) {
        return negozio.getTipo();
    }
}

