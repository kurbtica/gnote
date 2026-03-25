package com.stsau.slam2.API_Gnotes.service;

import com.stsau.slam2.API_Gnotes.model.Role;
import com.stsau.slam2.API_Gnotes.model.User;
import com.stsau.slam2.API_Gnotes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class CustomUserDetailsServiceTest {

	private CustomUserDetailsService customUserDetailsService;
	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setEmail("test@test.com");
		testUser.setPassword("password123");
		testUser.setRole(Role.ADMIN);

		UserRepository fakeRepo = new UserRepository() {
			@Override
			public Optional<User> findByEmail(String email) {
				if ("test@test.com".equals(email)) {
					return Optional.of(testUser);
				}
				return Optional.empty();
			}

			// Unused mock methods
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

		customUserDetailsService = new CustomUserDetailsService(fakeRepo);
	}

	@Test
	void testLoadUserByUsername_Success() {
		UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@test.com");

		assertNotNull(userDetails);
		assertEquals("test@test.com", userDetails.getUsername());
		assertEquals("password123", userDetails.getPassword());
		assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
	}

	@Test
	void testLoadUserByUsername_UserNotFound() {
		assertThrows(UsernameNotFoundException.class,
				() -> customUserDetailsService.loadUserByUsername("notfound@test.com"));
	}
}
