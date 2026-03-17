package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.exception.UserNotFoundException;
import com.stsau.slam2.API_Gnotes.model.Note;
import com.stsau.slam2.API_Gnotes.model.assembler.NoteModelAssembler;
import com.stsau.slam2.API_Gnotes.repository.NoteRepository;
import com.stsau.slam2.API_Gnotes.repository.UserRepository;
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

	private final NoteRepository noteRepository;
	private final NoteModelAssembler assembler;
	private final UserRepository userRepository;

	// 2. Changement du constructeur
	NoteController(NoteRepository repository, NoteModelAssembler assembler, UserRepository userRepository) {
		this.noteRepository = repository;
		this.assembler = assembler;
		this.userRepository = userRepository;
	}

	// Aggregate root
	@GetMapping("/notes")
	public CollectionModel<EntityModel<Note>> all() {
		// 3. Utilisation de l'assembler ici pour nettoyer le code
		// Cela remplace votre logique complexe où 'employee' et 'user' se mélangeaient
		List<EntityModel<Note>> notes = noteRepository.findAll().stream().map(assembler::toModel)
				.collect(Collectors.toList());

		return CollectionModel.of(notes, linkTo(methodOn(NoteController.class).all()).withSelfRel());
	}

	@PostMapping("/notes")
	ResponseEntity<?> newNote(@RequestBody Note newNote) {
		EntityModel<Note> entityModel = assembler.toModel(noteRepository.save(newNote));

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	// Single item
	@GetMapping("/notes/{id}")
	public EntityModel<Note> one(@PathVariable Long id) {

		Note note = noteRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

		return assembler.toModel(note);
	}

	@PutMapping("/notes/{id}")
	ResponseEntity<?> replaceNote(@RequestBody Note newNote, @PathVariable Long id) {

		Note updatedNote = noteRepository.findById(id).map(note -> {
			// Attention: assurez-vous que setName gère bien nom/prénom dans User.java
			note.setEleve(newNote.getEleve());
			note.setValeur(newNote.getValeur());
			note.setModification(newNote.getModification());
			note.setModification(newNote.getModification());
			return noteRepository.save(note);
		}).orElseGet(() -> {
			// Si l'ID n'existe pas, on le définit pour le nouvel utilisateur
			newNote.setId(id);
			return noteRepository.save(newNote);
		});

		EntityModel<Note> entityModel = assembler.toModel(updatedNote);

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@DeleteMapping("/notes/{id}")
	void deleteNote(@PathVariable Long id) {
		noteRepository.deleteById(id);
	}

	@GetMapping("/api/users/{userId}/notes")
	public List<Note> allNotesForUser(@PathVariable Long userId) {
		// BONUS : Bonne pratique (Optionnel)
		// Vérifier si l'user existe avant de chercher ses notes
		// Si l'user n'existe pas, on renvoie une 404 (UserNotFoundException)
		if (!userRepository.existsById(userId)) {
			throw new UserNotFoundException(userId);
		}

		// Ici, on cherche juste avec l'ID (le Long), pas besoin de l'objet User
		return noteRepository.findByEleveId(userId);
	}

}