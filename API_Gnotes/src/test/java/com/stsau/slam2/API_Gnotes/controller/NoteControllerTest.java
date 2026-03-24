package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.model.Note;
import com.stsau.slam2.API_Gnotes.model.assembler.NoteModelAssembler;
import com.stsau.slam2.API_Gnotes.repository.NoteRepository;
import com.stsau.slam2.API_Gnotes.repository.UserRepository;
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
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class NoteControllerTest {

    private NoteController controller;
    private Note note;
    private boolean deleteCalled = false;

    @BeforeEach
    void setUp() {
        note = new Note();
        note.setId(1L);
        note.setValeur(15.0);

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        NoteModelAssembler fakeAssembler = new NoteModelAssembler() {
            @Override
            public EntityModel<Note> toModel(Note entity) {
                EntityModel<Note> model = EntityModel.of(entity);
                model.add(Link.of("http://localhost/notes/" + entity.getId(), "self"));
                return model;
            }
        };

        NoteRepository fakeRepo = new NoteRepository() {
            @Override public Optional<Note> findById(Long id) { return (id == 1L) ? Optional.of(note) : Optional.empty(); }
            @Override public List<Note> findByEleveId(Long eleveId) { return Arrays.asList(note); }
            @Override public List<Note> findAll() { return Arrays.asList(note); }
            @Override public <S extends Note> S save(S entity) { return entity; }
            @Override public void deleteById(Long id) { if(id == 1L) deleteCalled = true; }
            @Override public void flush() {}
            @Override public <S extends Note> S saveAndFlush(S entity) { return null; }
            @Override public <S extends Note> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<Note> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public Note getOne(Long id) { return null; }
            @Override public Note getById(Long id) { return null; }
            @Override public Note getReferenceById(Long id) { return null; }
            @Override public <S extends Note> List<S> findAll(Example<S> example) { return null; }
            @Override public <S extends Note> List<S> findAll(Example<S> example, Sort sort) { return null; }
            @Override public <S extends Note> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public List<Note> findAllById(Iterable<Long> ids) { return null; }
            @Override public long count() { return 0; }
            @Override public void delete(Note entity) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends Note> entities) {}
            @Override public void deleteAll() {}
            @Override public List<Note> findAll(Sort sort) { return null; }
            @Override public Page<Note> findAll(Pageable pageable) { return null; }
            @Override public <S extends Note> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
            @Override public <S extends Note> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
            @Override public <S extends Note> long count(Example<S> example) { return 0; }
            @Override public <S extends Note> boolean exists(Example<S> example) { return false; }
            @Override public <S extends Note, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public boolean existsById(Long id) { return id == 1L; }
        };

        UserRepository fakeUserRepo = new com.stsau.slam2.API_Gnotes.repository.UserRepository() {
            @Override public Optional<com.stsau.slam2.API_Gnotes.model.User> findByEmail(String email) { return Optional.empty(); }
            @Override public void flush() {}
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> S saveAndFlush(S entity) { return null; }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<com.stsau.slam2.API_Gnotes.model.User> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public com.stsau.slam2.API_Gnotes.model.User getOne(Long id) { return null; }
            @Override public com.stsau.slam2.API_Gnotes.model.User getById(Long id) { return null; }
            @Override public com.stsau.slam2.API_Gnotes.model.User getReferenceById(Long id) { return null; }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> List<S> findAll(Example<S> example) { return null; }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> List<S> findAll(Example<S> example, Sort sort) { return null; }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public List<com.stsau.slam2.API_Gnotes.model.User> findAll() { return null; }
            @Override public List<com.stsau.slam2.API_Gnotes.model.User> findAllById(Iterable<Long> ids) { return null; }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> S save(S entity) { return null; }
            @Override public Optional<com.stsau.slam2.API_Gnotes.model.User> findById(Long id) { return Optional.empty(); }
            @Override public boolean existsById(Long id) { return id == 1L; }
            @Override public long count() { return 0; }
            @Override public void deleteById(Long id) {}
            @Override public void delete(com.stsau.slam2.API_Gnotes.model.User entity) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends com.stsau.slam2.API_Gnotes.model.User> entities) {}
            @Override public void deleteAll() {}
            @Override public List<com.stsau.slam2.API_Gnotes.model.User> findAll(Sort sort) { return null; }
            @Override public Page<com.stsau.slam2.API_Gnotes.model.User> findAll(Pageable pageable) { return null; }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> long count(Example<S> example) { return 0; }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User> boolean exists(Example<S> example) { return false; }
            @Override public <S extends com.stsau.slam2.API_Gnotes.model.User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        };

        controller = new NoteController(fakeRepo, fakeAssembler, fakeUserRepo);
        deleteCalled = false;
    }

    @Test
    void testGetAll() {
        CollectionModel<EntityModel<Note>> result = controller.all();
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    void testGetOne() {
        EntityModel<Note> result = controller.one(1L);
        assertEquals(15.0, result.getContent().getValeur());
    }

    @Test
    void testNewNote() {
        ResponseEntity<?> resp = controller.newNote(note);
        assertEquals(201, resp.getStatusCode().value());
    }

    @Test
    void testDeleteNote() {
        controller.deleteNote(1L);
        assertTrue(deleteCalled);
    }

    @Test
    void testAllNotesForUser() {
        List<Note> result = controller.allNotesForUser(1L);
        assertFalse(result.isEmpty());
    }
}
