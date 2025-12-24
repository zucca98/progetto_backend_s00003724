package com.epicode.Progetto_Backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.lang.NonNull;

import graphql.schema.idl.RuntimeWiring;

/**
 * GraphQLConfig - Configurazione per GraphQL.
 * 
 * Questa classe configura il runtime wiring di GraphQL, che definisce come
 * vengono risolti i tipi e i campi dello schema GraphQL.
 * 
 * Attualmente la configurazione è minimale perché Spring GraphQL gestisce
 * automaticamente la conversione dei tipi standard (come LocalDate) tramite
 * i resolver definiti nelle classi QueryResolver e MutationResolver.
 * 
 * Questa classe può essere estesa per:
 * - Definire scalari personalizzati (es: Date, DateTime custom)
 * - Configurare resolver per tipi complessi
 * - Aggiungere directive personalizzate
 * - Configurare error handling personalizzato
 * 
 * L'endpoint GraphQL è esposto su: POST /graphql
 * L'UI GraphiQL è disponibile su: GET /graphiql (se abilitata)
 */
@Configuration
public class GraphQLConfig implements RuntimeWiringConfigurer {

    /**
     * Configura il runtime wiring di GraphQL.
     * 
     * Spring GraphQL gestisce automaticamente:
     * - Conversione di LocalDate a String (nel JSON di risposta)
     * - Binding dei parametri dagli argomenti GraphQL ai metodi Java
     * - Risoluzione dei campi attraverso i resolver (@QueryMapping, @MutationMapping)
     * 
     * @param builder Builder per configurare il runtime wiring
     */
    @Override
    public void configure(@NonNull RuntimeWiring.Builder builder) {
        // Spring GraphQL automatically handles LocalDate conversion
        // This configuration can be extended if needed for custom scalars
    }
}

