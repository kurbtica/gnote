package com.stsau.slam2.API_Gnotes.controller;


import com.stsau.slam2.API_Gnotes.exception.UserNotFoundException;
import com.stsau.slam2.API_Gnotes.model.Note;
import com.stsau.slam2.API_Gnotes.model.NoteModelAssembler;
import com.stsau.slam2.API_Gnotes.repository.NoteRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class NoteController { // 1. Changement de nom : EmployeeController -> UserController

    private final NoteRepository repository;
    private final NoteModelAssembler assembler;

    // 2. Changement du constructeur
    NoteController(NoteRepository repository, NoteModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    // Aggregate root
    @GetMapping("/notes")
    public CollectionModel<EntityModel<Note>> all() {
        // 3. Utilisation de l'assembler ici pour nettoyer le code
        // Cela remplace votre logique complexe où 'employee' et 'user' se mélangeaient
        List<EntityModel<Note>> notes = repository.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(notes,
                linkTo(methodOn(NoteController.class).all()).withSelfRel());
    }

    @PostMapping("/notes")
    ResponseEntity<?> newNote(@RequestBody Note newNote) {
        EntityModel<Note> entityModel = assembler.toModel(repository.save(newNote));

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    // Single item
    @GetMapping("/notes/{id}")
    public EntityModel<Note> one(@PathVariable Long id) {

        Note note = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return assembler.toModel(note);
    }

    @PutMapping("/notes/{id}")
    ResponseEntity<?> replaceNote(@RequestBody Note newNote, @PathVariable Long id) {

        Note updatedNote = repository.findById(id)
                .map(note -> {
                    // Attention: assurez-vous que setName gère bien nom/prénom dans User.java
                    note.setId_enseignant(newNote.getId_enseignant());
                    note.setId_eleve(newNote.getId_eleve());
                    note.setId_matiere(newNote.getId_matiere()); // Ajouté pour cohérence
                    note.setId_type(newNote.getId_type()); // Ajouté pour cohérence
                    note.setValeur(newNote.getValeur());
                    note.setCoefficient(newNote.getCoefficient());
                    note.setId_appreciation(newNote.getId_appreciation());
                    note.setDate(newNote.getDate());
                    note.setDate_modif(newNote.getDate_modif());// Ajouté pour cohérence
                    return repository.save(note);
                })
                .orElseGet(() -> {
                    // Si l'ID n'existe pas, on le définit pour le nouvel utilisateur
                    newNote.setId(id);
                    return repository.save(newNote);
                });

        EntityModel<Note> entityModel = assembler.toModel(updatedNote);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @DeleteMapping("/notes/{id}")
    void deleteNote(@PathVariable Long id) {
        repository.deleteById(id);
    }
}