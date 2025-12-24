package com.epicode.Progetto_Backend.graphql.input;

import lombok.Data;

/**
 * UserUpdateInput - Input type GraphQL per l'aggiornamento di utenti.
 * 
 * Utilizzato nelle mutation GraphQL:
 * - updateUser(id: ID!, input: UserUpdateInput!)
 * - updateMe(input: UserUpdateInput!)
 * 
 * Questo Input viene convertito in UserUpdateDTO dal MutationResolver
 * prima di essere passato al servizio.
 * 
 * Tutti i campi sono opzionali - vengono aggiornati solo i campi non null.
 * Il campo profileImage deve contenere un URL valido (solitamente da Cloudinary).
 * 
 * @see com.epicode.Progetto_Backend.graphql.MutationResolver
 * @see com.epicode.Progetto_Backend.dto.UserUpdateDTO
 */
@Data
public class UserUpdateInput {
    /** Nome dell'utente (opzionale) */
    private String nome;
    
    /** Cognome dell'utente (opzionale) */
    private String cognome;
    
    /** URL dell'immagine profilo (opzionale, solitamente da Cloudinary) */
    private String profileImage;
}
