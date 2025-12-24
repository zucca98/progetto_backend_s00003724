package com.epicode.Progetto_Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * SwaggerConfig - Configurazione per la documentazione API con Swagger/OpenAPI.
 * 
 * Questa classe configura Swagger UI per generare automaticamente la documentazione
 * interattiva delle API REST del progetto basandosi sulle annotazioni @Operation,
 * @ApiResponse, @Tag, etc. presenti nei controller.
 * 
 * Funzionalità configurate:
 * - Informazioni generali dell'API (titolo, versione, descrizione)
 * - Supporto per autenticazione JWT (Bearer Token)
 * - Licenza Apache 2.0
 * - Contatti per supporto
 * 
 * Accesso alla documentazione:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 * 
 * La documentazione Swagger è generalmente disponibile solo in ambiente di sviluppo.
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * Configura l'oggetto OpenAPI utilizzato da Swagger per generare la documentazione.
     * 
     * Include:
     * - Metadati dell'API (titolo, versione, descrizione)
     * - Schema di sicurezza JWT (Bearer Token)
     * - Informazioni di contatto e licenza
     * 
     * Tutti gli endpoint sono protetti con autenticazione JWT per default.
     * L'utente può inserire il token JWT ottenuto dal login nella UI di Swagger
     * usando il pulsante "Authorize".
     * 
     * @return OpenAPI configurato con tutte le informazioni e lo schema di sicurezza
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Progetto Backend API")
                        .version("1.0.0")
                        .description("API RESTful per la gestione di immobili, contratti, locatari e rate. " +
                                "Include anche endpoint GraphQL per query avanzate.")
                        .contact(new Contact()
                                .name("Support Team")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Inserisci il token JWT ottenuto dal login")));
    }
}

