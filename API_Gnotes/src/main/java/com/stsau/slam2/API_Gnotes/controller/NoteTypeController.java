package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.exception.UserNotFoundException;
import com.stsau.slam2.API_Gnotes.model.NoteType;
import com.stsau.slam2.API_Gnotes.model.assembler.NoteTypeModelAssembler;
import com.stsau.slam2.API_Gnotes.repository.NoteTypeRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class NoteTypeController { // 1. Changement de nom : EmployeeController -> UserController

	private final NoteTypeRepository repository;
	private final NoteTypeModelAssembler assembler;

	// 2. Changement du constructeur
	NoteTypeController(NoteTypeRepository repository, NoteTypeModelAssembler assembler) {
		this.repository = repository;
		this.assembler = assembler;
	}

	// Aggregate root
	@GetMapping("/api/notes/type")
	public CollectionModel<EntityModel<NoteType>> all() {
		// 3. Utilisation de l'assembler ici pour nettoyer le code
		// Cela remplace votre logique complexe où 'employee' et 'user' se mélangeaient
		List<EntityModel<NoteType>> notes = repository.findAll().stream().map(assembler::toModel)
				.collect(Collectors.toList());

		return CollectionModel.of(notes, linkTo(methodOn(NoteTypeController.class).all()).withSelfRel());
	}

	// Single item
	@GetMapping("/api/notes/type/{id}")
	public EntityModel<NoteType> one(@PathVariable Long id) {

		NoteType notetype = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

		return assembler.toModel(notetype);
	}
}
