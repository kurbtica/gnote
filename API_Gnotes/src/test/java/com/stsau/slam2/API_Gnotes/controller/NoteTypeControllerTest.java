package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.model.NoteType;
import com.stsau.slam2.API_Gnotes.model.assembler.NoteTypeModelAssembler;
import com.stsau.slam2.API_Gnotes.repository.NoteTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class NoteTypeControllerTest {

	private NoteTypeController controller;
	private NoteType type;

	@BeforeEach
	void setUp() {
		type = new NoteType();
		type.setId(1L);
		type.setLibelle("Examen");

		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		NoteTypeModelAssembler fakeAssembler = new NoteTypeModelAssembler() {
			@Override
			public EntityModel<NoteType> toModel(NoteType entity) {
				EntityModel<NoteType> model = EntityModel.of(entity);
				model.add(Link.of("http://localhost/api/notes/type/" + entity.getId(), "self"));
				return model;
			}
		};

		NoteTypeRepository fakeRepo = new NoteTypeRepository() {
			@Override
			public Optional<NoteType> findById(Long id) {
				return (id == 1L) ? Optional.of(type) : Optional.empty();
			}
			@Override
			public List<NoteType> findAll() {
				return Arrays.asList(type);
			}
			@Override
			public <S extends NoteType> S save(S entity) {
				return entity;
			}
			@Override
			public void flush() {
			}
			@Override
			public <S extends NoteType> S saveAndFlush(S entity) {
				return null;
			}
			@Override
			public <S extends NoteType> List<S> saveAllAndFlush(Iterable<S> entities) {
				return null;
			}
			@Override
			public void deleteAllInBatch(Iterable<NoteType> entities) {
			}
			@Override
			public void deleteAllByIdInBatch(Iterable<Long> ids) {
			}
			@Override
			public void deleteAllInBatch() {
			}
			@Override
			public NoteType getOne(Long id) {
				return null;
			}
			@Override
			public NoteType getById(Long id) {
				return null;
			}
			@Override
			public NoteType getReferenceById(Long id) {
				return null;
			}
			@Override
			public <S extends NoteType> List<S> findAll(Example<S> example) {
				return null;
			}
			@Override
			public <S extends NoteType> List<S> findAll(Example<S> example, Sort sort) {
				return null;
			}
			@Override
			public <S extends NoteType> List<S> saveAll(Iterable<S> entities) {
				return null;
			}
			@Override
			public List<NoteType> findAllById(Iterable<Long> ids) {
				return null;
			}
			@Override
			public long count() {
				return 0;
			}
			@Override
			public void deleteById(Long id) {
			}
			@Override
			public void delete(NoteType entity) {
			}
			@Override
			public void deleteAllById(Iterable<? extends Long> ids) {
			}
			@Override
			public void deleteAll(Iterable<? extends NoteType> entities) {
			}
			@Override
			public void deleteAll() {
			}
			@Override
			public List<NoteType> findAll(Sort sort) {
				return null;
			}
			@Override
			public Page<NoteType> findAll(Pageable pageable) {
				return null;
			}
			@Override
			public <S extends NoteType> Optional<S> findOne(Example<S> example) {
				return Optional.empty();
			}
			@Override
			public <S extends NoteType> Page<S> findAll(Example<S> example, Pageable pageable) {
				return null;
			}
			@Override
			public <S extends NoteType> long count(Example<S> example) {
				return 0;
			}
			@Override
			public <S extends NoteType> boolean exists(Example<S> example) {
				return false;
			}
			@Override
			public <S extends NoteType, R> R findBy(Example<S> example,
					Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
				return null;
			}
			@Override
			public boolean existsById(Long id) {
				return id == 1L;
			}
		};

		controller = new NoteTypeController(fakeRepo, fakeAssembler);
	}

	@Test
	void testGetAll() {
		CollectionModel<EntityModel<NoteType>> result = controller.all();
		assertFalse(result.getContent().isEmpty());
	}

	@Test
	void testGetOne() {
		EntityModel<NoteType> result = controller.one(1L);
		assertEquals("Examen", result.getContent().getLibelle());
	}
}
