package com.epicode.Progetto_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AlexApplication - Classe principale dell'applicazione Spring Boot.
 * 
 * Questa è la classe entry point dell'applicazione che avvia il contesto Spring.
 * 
 * Annotazioni:
 * - @SpringBootApplication: Abilita la configurazione automatica di Spring Boot,
 *   il component scanning e la configurazione di @EnableAutoConfiguration,
 *   @ComponentScan e @Configuration. Scansiona automaticamente i componenti
 *   nel package corrente e nei sottopackage.
 * 
 * - @EnableAsync: Abilita il supporto per l'esecuzione asincrona di metodi
 *   annotati con @Async. Utilizzato principalmente da MailgunService per
 *   l'invio di email in modo asincrono senza bloccare il thread principale.
 * 
 * Funzionalità principali dell'applicazione:
 * - API REST per gestione immobili, contratti, locatari, rate, manutenzioni
 * - API GraphQL per query e mutation alternative
 * - Autenticazione JWT stateless
 * - Autorizzazione basata su ruoli (ADMIN, MANAGER, LOCATARIO)
 * - Upload immagini su Cloudinary
 * - Invio email tramite Mailgun
 * - Documentazione API con Swagger/OpenAPI
 * 
 * All'avvio, Spring Boot:
 * 1. Carica la configurazione da application.properties
 * 2. Scansiona e registra i componenti (@Service, @Repository, @Controller, etc.)
 * 3. Configura il DataSource e JPA/Hibernate
 * 4. Esegue DataSeeder per popolare il database se vuoto
 * 5. Configura Spring Security con JWT
 * 6. Avvia il server embedded Tomcat (porta 8080 di default)
 * 
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.scheduling.annotation.EnableAsync
 */
@SpringBootApplication
@EnableAsync
public class AlexApplication {

	/**
	 * Metodo main - Entry point dell'applicazione.
	 * 
	 * Avvia l'applicazione Spring Boot creando il contesto dell'applicazione
	 * e avviando il server embedded.
	 * 
	 * @param args Argomenti della riga di comando (non utilizzati in questo caso)
	 */
	public static void main(String[] args) {
		SpringApplication.run(AlexApplication.class, args);
	}

}
