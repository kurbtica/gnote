package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.exception.UserNotFoundException;
import com.stsau.slam2.API_Gnotes.model.Matiere;
import com.stsau.slam2.API_Gnotes.model.assembler.MatiereModelAssembler;
import com.stsau.slam2.API_Gnotes.repository.MatiereRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class MatiereController { // 1. Changement de nom : EmployeeController -> UserController

	private final MatiereRepository repository;
	private final MatiereModelAssembler assembler;

	// 2. Changement du constructeur
	MatiereController(MatiereRepository repository, MatiereModelAssembler assembler) {
		this.repository = repository;
		this.assembler = assembler;
	}

	// Aggregate root
	@GetMapping("/api/matieres")
	public CollectionModel<EntityModel<Matiere>> all() {
		// 3. Utilisation de l'assembler ici pour nettoyer le code
		// Cela remplace votre logique complexe où 'employee' et 'user' se mélangeaient
		List<EntityModel<Matiere>> matireres = repository.findAll().stream().map(assembler::toModel)
				.collect(Collectors.toList());

		return CollectionModel.of(matireres, linkTo(methodOn(MatiereController.class).all()).withSelfRel());
	}

	@PostMapping("/api/matieres")
	ResponseEntity<?> newMatiere(@RequestBody Matiere newMatiere) {
		EntityModel<Matiere> entityModel = assembler.toModel(repository.save(newMatiere));

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	// Single item
	@GetMapping("/api/matieres/{id}")
	public EntityModel<Matiere> one(@PathVariable Long id) {

		Matiere matiere = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

		return assembler.toModel(matiere);
	}

	@PutMapping("/api/matieres/{id}")
	ResponseEntity<?> replaceUser(@RequestBody Matiere newMatiere, @PathVariable Long id) {

		Optional<Matiere> existing = repository.findById(id);

		Matiere updatedMatiere = existing.map(matiere -> {
			// Attention: assurez-vous que setName gère bien nom/prénom dans User.java
			matiere.setId(newMatiere.getId());
			matiere.setLibelle(newMatiere.getLibelle());
			// Ajouté pour cohérence
			return repository.save(matiere);
		}).orElseGet(() -> {
			// Si l'ID n'existe pas, on le définit pour le nouvel utilisateur
			newMatiere.setId(id);
			return repository.save(newMatiere);
		});

		EntityModel<Matiere> entityModel = assembler.toModel(updatedMatiere);

		if (existing.isPresent()) {
			// Cas mise à jour → 200 OK
			return ResponseEntity.ok(entityModel);
		} else {
			// Cas création → 201 Created
			return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
					.body(entityModel);
		}
	}

	@DeleteMapping("/api/matieres/{id}")
	void deleteUser(@PathVariable Long id) {
		repository.deleteById(id);
	}
}