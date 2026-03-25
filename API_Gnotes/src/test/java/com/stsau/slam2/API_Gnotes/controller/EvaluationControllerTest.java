package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.model.Evaluation;
import com.stsau.slam2.API_Gnotes.model.Matiere;
import com.stsau.slam2.API_Gnotes.model.User;
import com.stsau.slam2.API_Gnotes.model.assembler.EvaluationModelAssembler;
import com.stsau.slam2.API_Gnotes.repository.EvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationControllerTest {

	private EvaluationController evaluationController;
	private Evaluation evaluation;
	private boolean deleteCalled = false;

	@BeforeEach
	void setUp() {
		evaluation = new Evaluation();
		evaluation.setId(1L);
		evaluation.setTitre("Evaluation Test");
		evaluation.setCoefficient(2.0);
		evaluation.setDate("2026-03-24");

		Matiere matiere = new Matiere();
		matiere.setId(1L);
		matiere.setLibelle("Maths");
		evaluation.setMatiere(matiere);

		User user = new User();
		user.setId(1L);
		user.setEmail("prof@test.com");
		evaluation.setEnseignant(user);

		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		EvaluationModelAssembler fakeAssembler = new EvaluationModelAssembler() {
			@Override
			public EntityModel<Evaluation> toModel(Evaluation ev) {
				EntityModel<Evaluation> model = EntityModel.of(ev);
				model.add(Link.of("http://localhost/api/evaluations/" + ev.getId(), "self"));
				return model;
			}
		};

		EvaluationRepository fakeRepo = new EvaluationRepository() {
			@Override
			public Optional<Evaluation> findById(Long id) {
				if (id == 1L)
					return Optional.of(evaluation);
				return Optional.empty();
			}
			@Override
			public List<Evaluation> findAll() {
				return Arrays.asList(evaluation);
			}
			@Override
			public <S extends Evaluation> S save(S entity) {
				return entity;
			}
			@Override
			public void deleteById(Long id) {
				if (id == 1L)
					deleteCalled = true;
			}
			// Dummy implementations for the rest
			@Override
			public void flush() {
			}
			@Override
			public <S extends Evaluation> S saveAndFlush(S entity) {
				return null;
			}
			@Override
			public <S extends Evaluation> List<S> saveAllAndFlush(Iterable<S> entities) {
				return null;
			}
			@Override
			public void deleteAllInBatch(Iterable<Evaluation> entities) {
			}
			@Override
			public void deleteAllByIdInBatch(Iterable<Long> ids) {
			}
			@Override
			public void deleteAllInBatch() {
			}
			@Override
			public Evaluation getOne(Long id) {
				return null;
			}
			@Override
			public Evaluation getById(Long id) {
				return null;
			}
			@Override
			public Evaluation getReferenceById(Long id) {
				return null;
			}
			@Override
			public <S extends Evaluation> List<S> findAll(Example<S> example) {
				return null;
			}
			@Override
			public <S extends Evaluation> List<S> findAll(Example<S> example, Sort sort) {
				return null;
			}
			@Override
			public <S extends Evaluation> List<S> saveAll(Iterable<S> entities) {
				return null;
			}
			@Override
			public List<Evaluation> findAllById(Iterable<Long> ids) {
				return null;
			}
			@Override
			public long count() {
				return 0;
			}
			@Override
			public void delete(Evaluation entity) {
			}
			@Override
			public void deleteAllById(Iterable<? extends Long> ids) {
			}
			@Override
			public void deleteAll(Iterable<? extends Evaluation> entities) {
			}
			@Override
			public void deleteAll() {
			}
			@Override
			public List<Evaluation> findAll(Sort sort) {
				return null;
			}
			@Override
			public Page<Evaluation> findAll(Pageable pageable) {
				return null;
			}
			@Override
			public <S extends Evaluation> Optional<S> findOne(Example<S> example) {
				return Optional.empty();
			}
			@Override
			public <S extends Evaluation> Page<S> findAll(Example<S> example, Pageable pageable) {
				return null;
			}
			@Override
			public <S extends Evaluation> long count(Example<S> example) {
				return 0;
			}
			@Override
			public <S extends Evaluation> boolean exists(Example<S> example) {
				return false;
			}
			@Override
			public <S extends Evaluation, R> R findBy(Example<S> example,
					Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
				return null;
			}
			@Override
			public boolean existsById(Long id) {
				return false;
			}
		};

		evaluationController = new EvaluationController(fakeRepo, fakeAssembler);
		deleteCalled = false;
	}

	@Test
	public void testGetEvaluationById() {
		EntityModel<Evaluation> response = evaluationController.one(1L);
		assertNotNull(response);
		assertEquals(evaluation, response.getContent());
	}

	@Test
	public void testGetAllEvaluations() {
		CollectionModel<EntityModel<Evaluation>> response = evaluationController.all();
		assertNotNull(response);
		assertFalse(response.getContent().isEmpty());
	}

	@Test
	public void testCreateEvaluation() {
		ResponseEntity<?> response = evaluationController.newEvaluation(evaluation);
		assertEquals(201, response.getStatusCode().value());
		assertTrue(response.getHeaders().getLocation().toString().endsWith("/api/evaluations/1"));
	}

	@Test
	public void testDeleteEvaluation() {
		assertDoesNotThrow(() -> evaluationController.deleteEvaluation(1L));
		assertTrue(deleteCalled);
	}
}
