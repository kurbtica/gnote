package com.stsau.slam2.API_Gnotes.repository;


import com.stsau.slam2.API_Gnotes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // 1. Pour la route /api/users/{id}/notes
    // Spring comprend : "Cherche les Notes où le champ 'eleve' a un 'id' égal au paramètre"
    List<Note> findByEleveId(Long eleveId);

    // 2. Pour la route /api/users/{id}/notes/{noteId}
    // Spring comprend : "Cherche la Note par son ID ET vérifie que l'élève correspond"
    Optional<Note> findByIdAndEleveId(Long id, Long eleveId);
}

