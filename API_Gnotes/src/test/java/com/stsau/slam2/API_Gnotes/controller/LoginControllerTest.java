package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.model.User;
import com.stsau.slam2.API_Gnotes.repository.UserRepository;
import com.stsau.slam2.API_Gnotes.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LoginControllerTest {

	private LoginController loginController;
	private User fakeUser;

	@BeforeEach
	void setUp() {
		fakeUser = new User();
		fakeUser.setId(1L);
		fakeUser.setEmail("test@test.com");
		fakeUser.setNom("Doe");

		AuthenticationManager fakeAuthManager = authentication -> {
			if ("test@test.com".equals(authentication.getPrincipal())
					&& "password".equals(authentication.getCredentials())) {
				return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
						authentication.getCredentials(), authentication.getAuthorities());
			}
			throw new RuntimeException("Bad credentials");
		};

		JwtService fakeJwtService = new JwtService() {
			@Override
			public String generateToken(Authentication authentication) {
				return "fake.jwt.token";
			}
		};

		UserRepository fakeUserRepo = new UserRepository() {
			@Override
			public Optional<User> findByEmail(String email) {
				if ("test@test.com".equals(email))
					return Optional.of(fakeUser);
				return Optional.empty();
			}
			// Mocks minimums obligatoires
			@Override
			public void flush() {
			}
			@Override
			public <S extends User> S saveAndFlush(S entity) {
				return null;
			}
			@Override
			public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) {
				return null;
			}
			@Override
			public void deleteAllInBatch(Iterable<User> entities) {
			}
			@Override
			public void deleteAllByIdInBatch(Iterable<Long> ids) {
			}
			@Override
			public void deleteAllInBatch() {
			}
			@Override
			public User getOne(Long id) {
				return null;
			}
			@Override
			public User getById(Long id) {
				return null;
			}
			@Override
			public User getReferenceById(Long id) {
				return null;
			}
			@Override
			public <S extends User> List<S> findAll(Example<S> example) {
				return null;
			}
			@Override
			public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
				return null;
			}
			@Override
			public <S extends User> List<S> saveAll(Iterable<S> entities) {
				return null;
			}
			@Override
			public List<User> findAll() {
				return null;
			}
			@Override
			public List<User> findAllById(Iterable<Long> ids) {
				return null;
			}
			@Override
			public <S extends User> S save(S entity) {
				return null;
			}
			@Override
			public Optional<User> findById(Long id) {
				return Optional.empty();
			}
			@Override
			public boolean existsById(Long id) {
				return false;
			}
			@Override
			public long count() {
				return 0;
			}
			@Override
			public void deleteById(Long id) {
			}
			@Override
			public void delete(User entity) {
			}
			@Override
			public void deleteAllById(Iterable<? extends Long> ids) {
			}
			@Override
			public void deleteAll(Iterable<? extends User> entities) {
			}
			@Override
			public void deleteAll() {
			}
			@Override
			public List<User> findAll(Sort sort) {
				return null;
			}
			@Override
			public Page<User> findAll(Pageable pageable) {
				return null;
			}
			@Override
			public <S extends User> Optional<S> findOne(Example<S> example) {
				return Optional.empty();
			}
			@Override
			public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
				return null;
			}
			@Override
			public <S extends User> long count(Example<S> example) {
				return 0;
			}
			@Override
			public <S extends User> boolean exists(Example<S> example) {
				return false;
			}
			@Override
			public <S extends User, R> R findBy(Example<S> example,
					Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
				return null;
			}
		};

		loginController = new LoginController(fakeAuthManager, fakeJwtService, fakeUserRepo);
	}

	@Test
	void testLoginWithJsonRequest() {
		LoginController.LoginRequest request = new LoginController.LoginRequest("test@test.com", "password");

		ResponseEntity<Map<String, Object>> response = loginController.login(request, null);

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals("fake.jwt.token", response.getBody().get("token"));
		assertEquals("Doe", response.getBody().get("nom"));
	}

	@Test
	void testLoginWithExistingAuth() {
		Authentication existingAuth = new UsernamePasswordAuthenticationToken("test@test.com", "password",
				java.util.Collections.emptyList());

		ResponseEntity<Map<String, Object>> response = loginController.login(null, existingAuth);

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals("fake.jwt.token", response.getBody().get("token"));
		assertEquals("Doe", response.getBody().get("nom"));
	}
}
