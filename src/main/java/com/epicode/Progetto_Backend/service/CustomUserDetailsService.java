package com.epicode.Progetto_Backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.epicode.Progetto_Backend.entity.User;
import com.epicode.Progetto_Backend.exception.EntityNotFoundException;
import com.epicode.Progetto_Backend.repository.UserRepository;

/**
 * CustomUserDetailsService - Implementazione di UserDetailsService per Spring Security.
 * 
 * Fornisce l'integrazione tra Spring Security e il sistema di autenticazione personalizzato.
 * Carica gli utenti dal database utilizzando UserRepository.
 * 
 * Metodi:
 * - loadUserByUsername: Carica un utente per email (username) - richiesto da UserDetailsService
 * - loadUserById: Carica un utente per ID (metodo aggiuntivo)
 * 
 * L'utente restituito implementa UserDetails e contiene:
 * - Credenziali (email, password hashata)
 * - Autorità (ruoli)
 * - Stato account (enabled, accountNonExpired, etc.)
 * 
 * Utilizzato da:
 * - JwtAuthenticationFilter per caricare i dettagli utente durante l'autenticazione JWT
 * - AuthenticationManager per validare le credenziali durante il login
 * 
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see com.epicode.Progetto_Backend.entity.User
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Carica un utente per username (email).
     * 
     * Metodo richiesto da UserDetailsService, utilizzato da Spring Security
     * per caricare i dettagli dell'utente durante l'autenticazione.
     * 
     * @param username Email dell'utente (utilizzata come username)
     * @return UserDetails con credenziali, ruoli e stato account
     * @throws UsernameNotFoundException se l'utente non viene trovato
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
    
    /**
     * Carica un utente per ID.
     * 
     * Metodo aggiuntivo per caricare un utente tramite ID invece che email.
     * 
     * @param id ID dell'utente
     * @return UserDetails con credenziali, ruoli e stato account
     * @throws EntityNotFoundException se l'utente non viene trovato
     */
    @SuppressWarnings("null")
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        return user;
    }
}
