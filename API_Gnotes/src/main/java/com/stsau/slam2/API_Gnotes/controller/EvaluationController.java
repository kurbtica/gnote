package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.exception.EvaluationNotFoundException;
import com.stsau.slam2.API_Gnotes.model.Evaluation;
import com.stsau.slam2.API_Gnotes.model.Note;
import com.stsau.slam2.API_Gnotes.model.assembler.EvaluationModelAssembler;
import com.stsau.slam2.API_Gnotes.repository.EvaluationRepository;
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
public class EvaluationController {
	private final EvaluationRepository repository;
	private final EvaluationModelAssembler assembler;

	EvaluationController(EvaluationRepository repository, EvaluationModelAssembler assembler) {
		this.repository = repository;
		this.assembler = assembler;
	}

	// Aggregate root
	@GetMapping("/api/evaluations")
	public CollectionModel<EntityModel<Evaluation>> all() {
		List<EntityModel<Evaluation>> evaluations = repository.findAll().stream().map(assembler::toModel)
				.collect(Collectors.toList());

		return CollectionModel.of(evaluations,
				linkTo(methodOn(com.stsau.slam2.API_Gnotes.controller.EvaluationController.class).all()).withSelfRel());
	}

	@PostMapping("/api/evaluations")
	ResponseEntity<?> newEvaluation(@RequestBody Evaluation newEvaluation) {
		// 1. Re-maillage : On lie chaque note à l'évaluation parente
		if (newEvaluation.getNotes() != null) {
			for (Note note : newEvaluation.getNotes()) {
				note.setEvaluation(newEvaluation);
			}
		}

		// 2. Sauvegarde (Hibernate verra maintenant les liens et ne jettera plus
		// d'exception)
		Evaluation savedEvaluation = repository.save(newEvaluation);

		// 3. Transformation en modèle HATEOAS
		EntityModel<Evaluation> entityModel = assembler.toModel(savedEvaluation);

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	// Single item
	@GetMapping("/api/evaluations/{id}")
	public EntityModel<Evaluation> one(@PathVariable Long id) {

		Evaluation evaluation = repository.findById(id).orElseThrow(() -> new EvaluationNotFoundException(id));

		return assembler.toModel(evaluation);
	}

	@PutMapping("/api/evaluations/{id}")
	ResponseEntity<?> replaceEvaluation(@RequestBody Evaluation newEvaluation, @PathVariable Long id) {

		Optional<Evaluation> existing = repository.findById(id);

		Evaluation updatedEvaluation = existing.map(evaluation -> {
			evaluation.setEnseignant(newEvaluation.getEnseignant());
			evaluation.setMatiere(newEvaluation.getMatiere());
			evaluation.setCoefficient(newEvaluation.getCoefficient());
			evaluation.setTitre(newEvaluation.getTitre());
			evaluation.setDate(newEvaluation.getDate());
			evaluation.setModification(newEvaluation.getModification());
			evaluation.setNoteType(newEvaluation.getNoteType());
			// evaluation.setNotes(newEvaluation.getNotes());
			updateNotesList(evaluation, newEvaluation.getNotes());
			return repository.save(evaluation);
		}).orElseGet(() -> {
			// Si l'ID n'existe pas, on le définit pour le nouvel utilisateur
			newEvaluation.setId(id);
			return repository.save(newEvaluation);
		});

		EntityModel<Evaluation> entityModel = assembler.toModel(updatedEvaluation);

		if (existing.isPresent()) {
			// Cas mise à jour → 200 OK
			return ResponseEntity.ok(entityModel);
		} else {
			// Cas création → 201 Created
			return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
					.body(entityModel);
		}
	}

	private void updateNotesList(Evaluation existingEvaluation, List<Note> incomingNotes) {
		// Vider la liste existante. Cela marque toutes les notes actuelles comme
		// "orphelines"
		// et elles seront supprimées de la base de données grâce à orphanRemoval=true.
		existingEvaluation.getNotes().clear();

		// Ajouter les notes de la requête à la liste (maintenant vide).
		if (incomingNotes != null) {
			for (Note incomingNote : incomingNotes) {
				// S'assurer que la note est bien liée à son évaluation parente
				incomingNote.setEvaluation(existingEvaluation);
				existingEvaluation.getNotes().add(incomingNote);
			}
		}
	}

	@DeleteMapping("/api/evaluations/{id}")
	void deleteEvaluation(@PathVariable Long id) {
		repository.deleteById(id);
	}
}
