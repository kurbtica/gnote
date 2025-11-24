package com.stsau.slam2.API_Gnotes.repository;


import com.stsau.slam2.API_Gnotes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {

}

