package com.stsau.slam2.API_Gnotes.controller;

import com.stsau.slam2.API_Gnotes.service.JwtService;
import com.stsau.slam2.API_Gnotes.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogoutControllerTest {

	private LogoutController logoutController;
	private boolean tokenBlacklisted = false;

	@BeforeEach
	void setUp() {
		TokenBlacklistService fakeBlacklistService = new TokenBlacklistService(new JwtService()) {
			@Override
			public void blacklistToken(String token) {
				if ("valid.jwt.token".equals(token)) {
					tokenBlacklisted = true;
				}
			}
		};

		logoutController = new LogoutController(fakeBlacklistService);
		tokenBlacklisted = false;
	}

	@Test
	void testLogoutSuccess() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer valid.jwt.token");

		ResponseEntity<String> response = logoutController.logout(request);

		assertEquals(200, response.getStatusCode().value());
		assertEquals("Token invalidé avec succès.", response.getBody());
		assertEquals(true, tokenBlacklisted);
	}

	@Test
	void testLogoutNoToken() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		ResponseEntity<String> response = logoutController.logout(request);

		assertEquals(400, response.getStatusCode().value());
		assertEquals("Pas de token fourni", response.getBody());
		assertEquals(false, tokenBlacklisted);
	}
}
