package com.epicode.Progetto_Backend.entity;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User - Entità per gli utenti del sistema.
 * 
 * Rappresenta un utente dell'applicazione con autenticazione e autorizzazione.
 * Implementa UserDetails di Spring Security per l'integrazione con il sistema di sicurezza.
 * 
 * Relazioni:
 * - Many-to-Many con Role: Un utente può avere più ruoli
 * - One-to-One con Locatario: Un utente può essere associato a un locatario (opzionale)
 * 
 * La password viene hashata con BCrypt e non viene mai serializzata in JSON.
 * Il campo enabled permette di disabilitare un utente senza eliminarlo.
 * 
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see com.epicode.Progetto_Backend.entity.Role
 * @see com.epicode.Progetto_Backend.entity.Locatario
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    
    /** ID univoco dell'utente */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Email dell'utente (univoca, utilizzata come username per il login) */
    @Column(nullable = false, unique = true)
    private String email;
    
    /** Password hashata con BCrypt (non serializzata in JSON per sicurezza) */
    @Column(nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;
    
    /** URL dell'immagine profilo (solitamente da Cloudinary, aggiornabile) */
    @Column(name = "profile_image")
    private String profileImage; // URL aggiornabile
    
    /** Nome dell'utente */
    @Column(nullable = false)
    private String nome;
    
    /** Cognome dell'utente */
    @Column(nullable = false)
    private String cognome;
    
    /** Data di registrazione (default: data corrente) */
    @Column(name = "registration_date")
    @Builder.Default
    private LocalDate registrationDate = LocalDate.now();
    
    /** Flag per abilitare/disabilitare l'utente (default: true) */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    /** Ruoli dell'utente (caricati eager per Spring Security) */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    /** Locatario associato a questo utente (relazione one-to-one opzionale) */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Locatario locatario;
    
    // Implementazione UserDetails per Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
