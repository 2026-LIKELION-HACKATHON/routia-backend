package com.routiaback.global.security;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.auth.application.AuthService;
import com.routiaback.auth.application.result.EmailDuplicateCheckResult;
import com.routiaback.auth.application.result.LoginResult;
import com.routiaback.auth.presentation.AuthController;
import com.routiaback.global.error.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties =
	"routia.cors.allowed-origins=https://frontend.example, https://preview.example")
class AuthSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtTokenProvider tokenProvider;

	@BeforeEach
	void setUpResponses() {
		given(authService.checkDuplicate("user@example.com"))
			.willReturn(new EmailDuplicateCheckResult(false));
		given(authService.login(new com.routiaback.auth.application.command.LoginCommand("user@example.com", "secret")))
			.willReturn(new LoginResult("access-token"));
	}

	@Test
	void permitsAllAuthEndpointsWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/auth/email/check-duplicate")
				.param("email", "user@example.com"))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/auth/email/verification-code")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com"}
					"""))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/auth/email/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","code":"123456"}
					"""))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","password":"secret","passwordConfirm":"secret","name":"Soeun"}
					"""))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","password":"secret"}
					"""))
			.andExpect(status().isOk());
	}

	@Test
	void rejectsProtectedEndpointWithoutTokenAsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/onboarding/progress"))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/users/1/push-devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"installationId\":\"test-fid\",\"platform\":\"WEB\"}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsProtectedEndpointWithInvalidTokenAsUnauthorized() throws Exception {
		given(tokenProvider.parseUserId("invalid-token"))
			.willThrow(new IllegalArgumentException("Invalid JWT"));

		mockMvc.perform(get("/api/v1/onboarding/progress")
				.header("Authorization", "Bearer invalid-token"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void authenticatesProtectedRequestWithValidToken() throws Exception {
		given(tokenProvider.parseUserId("valid-token")).willReturn(7L);

		mockMvc.perform(get("/api/v1/onboarding/progress")
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isNotFound());
	}

	@ParameterizedTest
	@ValueSource(strings = {"GET", "POST", "PATCH", "DELETE"})
	void permitsPreflightWithoutJwtForConfiguredOriginAndAuthorizationHeader(String method) throws Exception {
		mockMvc.perform(options("/api/v1/onboarding/progress")
				.header(HttpHeaders.ORIGIN, "https://frontend.example")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://frontend.example"))
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization"));
	}

	@Test
	void returnsCorsHeaderOnActualRequestFromConfiguredOrigin() throws Exception {
		mockMvc.perform(get("/api/v1/auth/email/check-duplicate")
				.param("email", "user@example.com")
				.header(HttpHeaders.ORIGIN, "https://preview.example"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://preview.example"));
	}

	@Test
	void rejectsPreflightFromOriginOutsideAllowlist() throws Exception {
		mockMvc.perform(options("/api/v1/onboarding/progress")
				.header(HttpHeaders.ORIGIN, "https://evil.example")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
			.andExpect(status().isForbidden())
			.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
	}
}
