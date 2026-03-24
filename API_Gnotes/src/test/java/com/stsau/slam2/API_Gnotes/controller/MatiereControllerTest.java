package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.model.Matiere;
import com.stsau.slam2.API_Gnotes.model.assembler.MatiereModelAssembler;
import com.stsau.slam2.API_Gnotes.repository.MatiereRepository;
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

public class MatiereControllerTest {

    private MatiereController controller;
    private Matiere matiere;
    private boolean deleteCalled = false;

    @BeforeEach
    void setUp() {
        matiere = new Matiere();
        matiere.setId(1L);
        matiere.setLibelle("Maths");

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        MatiereModelAssembler fakeAssembler = new MatiereModelAssembler() {
            @Override
            public EntityModel<Matiere> toModel(Matiere entity) {
                EntityModel<Matiere> model = EntityModel.of(entity);
                model.add(Link.of("http://localhost/api/matieres/" + entity.getId(), "self"));
                return model;
            }
        };

        MatiereRepository fakeRepo = new MatiereRepository() {
            @Override public Optional<Matiere> findById(Long id) {
                return (id == 1L) ? Optional.of(matiere) : Optional.empty();
            }
            @Override public List<Matiere> findAll() { return Arrays.asList(matiere); }
            @Override public <S extends Matiere> S save(S entity) { return entity; }
            @Override public void deleteById(Long id) { if(id == 1L) deleteCalled = true; }
            @Override public void flush() {}
            @Override public <S extends Matiere> S saveAndFlush(S entity) { return null; }
            @Override public <S extends Matiere> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<Matiere> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public Matiere getOne(Long id) { return null; }
            @Override public Matiere getById(Long id) { return null; }
            @Override public Matiere getReferenceById(Long id) { return null; }
            @Override public <S extends Matiere> List<S> findAll(Example<S> example) { return null; }
            @Override public <S extends Matiere> List<S> findAll(Example<S> example, Sort sort) { return null; }
            @Override public <S extends Matiere> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public List<Matiere> findAllById(Iterable<Long> ids) { return null; }
            @Override public long count() { return 0; }
            @Override public void delete(Matiere entity) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends Matiere> entities) {}
            @Override public void deleteAll() {}
            @Override public List<Matiere> findAll(Sort sort) { return null; }
            @Override public Page<Matiere> findAll(Pageable pageable) { return null; }
            @Override public <S extends Matiere> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
            @Override public <S extends Matiere> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
            @Override public <S extends Matiere> long count(Example<S> example) { return 0; }
            @Override public <S extends Matiere> boolean exists(Example<S> example) { return false; }
            @Override public <S extends Matiere, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public boolean existsById(Long id) { return false; }
        };

        controller = new MatiereController(fakeRepo, fakeAssembler);
        deleteCalled = false;
    }

    @Test
    void testGetAll() {
        CollectionModel<EntityModel<Matiere>> result = controller.all();
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    void testGetOne() {
        EntityModel<Matiere> result = controller.one(1L);
        assertEquals("Maths", result.getContent().getLibelle());
    }

    @Test
    void testNewMatiere() {
        ResponseEntity<?> resp = controller.newMatiere(matiere);
        assertEquals(201, resp.getStatusCode().value());
    }

    @Test
    void testDeleteMatiere() {
        controller.deleteUser(1L);
        assertTrue(deleteCalled);
    }
}
