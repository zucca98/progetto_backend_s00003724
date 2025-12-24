package com.epicode.Progetto_Backend.integration;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.Progetto_Backend.dto.AuthResponseDTO;
import com.epicode.Progetto_Backend.dto.LoginRequestDTO;
import com.epicode.Progetto_Backend.dto.RegisterRequestDTO;
import com.epicode.Progetto_Backend.entity.Immobile;
import com.epicode.Progetto_Backend.entity.Locatario;
import com.epicode.Progetto_Backend.entity.Role;
import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.repository.ImmobileRepository;
import com.epicode.Progetto_Backend.repository.LocatarioRepository;
import com.epicode.Progetto_Backend.repository.RoleRepository;
import com.epicode.Progetto_Backend.repository.UserRepository;
import com.epicode.Progetto_Backend.service.MailgunService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * BaseIntegrationTest - Classe base astratta per i test di integrazione end-to-end.
 * 
 * Questa classe fornisce funzionalità comuni e helper methods per tutti i test
 * di integrazione che verificano il funzionamento completo dell'applicazione
 * attraverso le chiamate HTTP simulate con MockMvc.
 * 
 * Caratteristiche principali:
 * - Configurazione completa dell'ambiente di test con Spring Boot Test
 * - Database in-memory H2 per isolare i test
 * - MockMvc per simulare richieste HTTP senza avviare un server reale
 * - Metodi helper per creare utenti, immobili, locatari di test
 * - Gestione dell'autenticazione tramite token JWT
 * - Mock del MailgunService per evitare chiamate reali all'API esterna
 * 
 * Annotazioni:
 * - @SpringBootTest: Carica il contesto completo dell'applicazione
 * - @ActiveProfiles("test"): Utilizza il profilo "test" (database H2, configurazioni di test)
 * - @Transactional: Ogni test viene eseguito in una transazione che viene rollbackata alla fine
 * - @AutoConfigureMockMvc: Configura automaticamente MockMvc per testare i controller
 * 
 * Metodi helper forniti:
 * - getAuthToken(): Ottiene un token JWT tramite login
 * - createTestUser(): Crea utenti di test con ruoli specifici
 * - createTestImmobile(): Crea immobili di test
 * - createTestLocatario(): Crea locatari di test associati a utenti
 * - registerAndGetToken(): Registra un nuovo utente e restituisce il token
 * - extractToken(): Estrae il token dalla risposta JSON
 * 
 * Tutti i test di integrazione dovrebbero estendere questa classe per utilizzare
 * le funzionalità comuni e mantenere la coerenza tra i test.
 * 
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.test.web.servlet.MockMvc
 * @see org.springframework.transaction.annotation.Transactional
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@SuppressWarnings({"null", "removal"})
public abstract class BaseIntegrationTest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected RoleRepository roleRepository;
    
    @Autowired
    protected PasswordEncoder passwordEncoder;
    
    @Autowired
    protected ImmobileRepository immobileRepository;
    
    @Autowired
    protected LocatarioRepository locatarioRepository;
    
    // Mock MailgunService per evitare chiamate reali all'API Mailgun durante i test di integrazione
    // Note: @MockBean is deprecated in Spring Boot 3.4+ but replacement @MockitoBean not available in 3.5.9
    @MockBean
    protected MailgunService mailgunService;
    
    /**
     * Ottiene un token JWT per un utente esistente effettuando il login.
     * 
     * Questo metodo simula una richiesta POST a /api/auth/login e estrae
     * il token JWT dalla risposta. L'utente deve esistere nel database
     * prima di chiamare questo metodo.
     * 
     * @param email L'email dell'utente per il login
     * @param password La password dell'utente per il login
     * @return Il token JWT ottenuto dalla risposta di login
     * @throws Exception Se il login fallisce o la richiesta HTTP non va a buon fine
     */
    protected String getAuthToken(String email, String password) throws Exception {
        // Prova prima il login
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        AuthResponseDTO authResponse = objectMapper.readValue(response, AuthResponseDTO.class);
        return authResponse.getToken();
    }
    
    /**
     * Crea un utente di test con il ruolo specificato e lo salva nel database.
     * 
     * Se il ruolo non esiste, viene creato automaticamente. La password
     * viene codificata usando il PasswordEncoder prima di essere salvata.
     * 
     * @param email L'email dell'utente di test
     * @param password La password in chiaro (verrà codificata automaticamente)
     * @param roleName Il nome del ruolo (es. "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_LOCATARIO")
     * @return L'utente creato e salvato nel database
     */
    protected User createTestUser(String email, String password, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = Role.builder().name(roleName).build();
                    return roleRepository.save(newRole);
                });
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nome("Test")
                .cognome("User")
                .enabled(true)
                .roles(roles)
                .build();
        
        return userRepository.save(user);
    }
    
    /**
     * Crea un utente di test con ruolo LOCATARIO (default).
     * 
     * Versione semplificata di createTestUser che assegna automaticamente
     * il ruolo ROLE_LOCATARIO all'utente.
     * 
     * @param email L'email dell'utente di test
     * @param password La password in chiaro (verrà codificata automaticamente)
     * @return L'utente creato con ruolo LOCATARIO
     */
    protected User createTestUser(String email, String password) {
        return createTestUser(email, password, "ROLE_LOCATARIO");
    }
    
    /**
     * Crea un immobile di test di tipo APPARTAMENTO e lo salva nel database.
     * 
     * Viene creato un immobile base con valori di default (superficie 100.0 mq,
     * tipo APPARTAMENTO) che può essere utilizzato nei test. Per immobili
     * più complessi (Appartamento, Negozio, Ufficio con campi specifici),
     * è necessario crearli direttamente nei test.
     * 
     * @param indirizzo L'indirizzo dell'immobile
     * @param citta La città dell'immobile
     * @return L'immobile creato e salvato nel database
     */
    protected Immobile createTestImmobile(String indirizzo, String citta) {
        Immobile immobile = new Immobile();
        immobile.setIndirizzo(indirizzo);
        immobile.setCitta(citta);
        immobile.setSuperficie(100.0);
        immobile.setTipo(com.epicode.Progetto_Backend.entity.TipoImmobile.APPARTAMENTO);
        return immobileRepository.save(immobile);
    }
    
    /**
     * Crea un locatario di test associato a un utente e lo salva nel database.
     * 
     * Il locatario viene creato con dati di test standard (nome "Test",
     * cognome "Locatario", indirizzo "Via Test 123", telefono "1234567890")
     * e associato all'utente specificato. Il codice fiscale deve essere
     * fornito come parametro per garantire l'unicità.
     * 
     * @param user L'utente a cui associare il locatario (non può essere null)
     * @param cf Il codice fiscale del locatario (deve essere unico)
     * @return Il locatario creato e salvato nel database
     */
    protected Locatario createTestLocatario(User user, String cf) {
        Locatario locatario = Locatario.builder()
                .nome("Test")
                .cognome("Locatario")
                .cf(cf)
                .indirizzo("Via Test 123")
                .telefono("1234567890")
                .user(user)
                .build();
        return locatarioRepository.save(locatario);
    }
    
    /**
     * Registra un nuovo utente tramite l'endpoint /api/auth/register e restituisce il token JWT.
     * 
     * Questo metodo simula una richiesta di registrazione completa e estrae
     * il token dalla risposta. È utile per test che richiedono un utente
     * appena registrato con il token corrispondente.
     * 
     * @param email L'email del nuovo utente
     * @param password La password in chiaro (verrà codificata dal servizio)
     * @param nome Il nome dell'utente
     * @param cognome Il cognome dell'utente
     * @return Il token JWT ottenuto dalla risposta di registrazione
     * @throws Exception Se la registrazione fallisce o la richiesta HTTP non va a buon fine
     */
    protected String registerAndGetToken(String email, String password, String nome, String cognome) throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setNome(nome);
        registerRequest.setCognome(cognome);
        
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        AuthResponseDTO authResponse = objectMapper.readValue(response, AuthResponseDTO.class);
        return authResponse.getToken();
    }
    
    /**
     * Estrae il token JWT dalla risposta JSON di autenticazione.
     * 
     * Questo metodo helper deserializza la risposta JSON in un AuthResponseDTO
     * e restituisce il campo token. È utile quando si ha già un MvcResult
     * e si vuole estrarre solo il token senza rifare la deserializzazione completa.
     * 
     * @param result Il risultato della chiamata MockMvc contenente la risposta JSON
     * @return Il token JWT estratto dalla risposta
     * @throws Exception Se la deserializzazione JSON fallisce
     */
    protected String extractToken(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        AuthResponseDTO authResponse = objectMapper.readValue(response, AuthResponseDTO.class);
        return authResponse.getToken();
    }
}

