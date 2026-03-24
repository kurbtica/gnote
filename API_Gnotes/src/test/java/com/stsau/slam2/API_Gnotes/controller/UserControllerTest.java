package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.model.User;
import com.stsau.slam2.API_Gnotes.model.Role;
import com.stsau.slam2.API_Gnotes.model.assembler.UserModelAssembler;
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

public class UserControllerTest {

    private UserController controller;
    private User user;
    private boolean deleteCalled = false;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@test.com");
        user.setRole(Role.Administrateur);

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UserModelAssembler fakeAssembler = new UserModelAssembler() {
            @Override
            public EntityModel<User> toModel(User entity) {
                EntityModel<User> model = EntityModel.of(entity);
                model.add(Link.of("http://localhost/api/users/" + entity.getId(), "self"));
                return model;
            }
        };

        UserRepository fakeRepo = new UserRepository() {
            @Override public Optional<User> findById(Long id) { return (id == 1L) ? Optional.of(user) : Optional.empty(); }
            @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
            @Override public List<User> findAll() { return Arrays.asList(user); }
            @Override public <S extends User> S save(S entity) { return entity; }
            @Override public void deleteById(Long id) { if(id == 1L) deleteCalled = true; }
            @Override public void flush() {}
            @Override public <S extends User> S saveAndFlush(S entity) { return null; }
            @Override public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<User> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public User getOne(Long id) { return null; }
            @Override public User getById(Long id) { return null; }
            @Override public User getReferenceById(Long id) { return null; }
            @Override public <S extends User> List<S> findAll(Example<S> example) { return null; }
            @Override public <S extends User> List<S> findAll(Example<S> example, Sort sort) { return null; }
            @Override public <S extends User> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public List<User> findAllById(Iterable<Long> ids) { return null; }
            @Override public long count() { return 0; }
            @Override public void delete(User entity) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends User> entities) {}
            @Override public void deleteAll() {}
            @Override public List<User> findAll(Sort sort) { return null; }
            @Override public Page<User> findAll(Pageable pageable) { return null; }
            @Override public <S extends User> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
            @Override public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
            @Override public <S extends User> long count(Example<S> example) { return 0; }
            @Override public <S extends User> boolean exists(Example<S> example) { return false; }
            @Override public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public boolean existsById(Long id) { return id == 1L; }
        };

        controller = new UserController(fakeRepo, fakeAssembler);
        deleteCalled = false;
    }

    @Test
    void testGetAll() {
        CollectionModel<EntityModel<User>> result = controller.all();
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    void testGetOne() {
        EntityModel<User> result = controller.one(1L);
        assertEquals("John Doe", result.getContent().getName());
    }

    @Test
    void testNewUser() {
        ResponseEntity<?> resp = controller.newUser(user);
        assertEquals(201, resp.getStatusCode().value());
    }

    @Test
    void testDeleteUser() {
        controller.deleteUser(1L);
        assertTrue(deleteCalled);
    }
}
