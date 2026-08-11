package com.routiaback.auth.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.auth.application.AuthService;
import com.routiaback.auth.application.command.EmailVerificationCodeCommand;
import com.routiaback.auth.application.command.EmailVerifyCommand;
import com.routiaback.auth.application.command.LoginCommand;
import com.routiaback.auth.application.command.SignupCommand;
import com.routiaback.auth.application.result.EmailDuplicateCheckResult;
import com.routiaback.auth.application.result.LoginResult;
import com.routiaback.global.error.GlobalExceptionHandler;
import com.routiaback.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void checksEmailDuplication() throws Exception {
		given(authService.checkDuplicate("user@example.com"))
			.willReturn(new EmailDuplicateCheckResult(false));

		mockMvc.perform(get("/api/v1/auth/email/check-duplicate")
				.param("email", "user@example.com"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.duplicated").value(false))
			.andExpect(jsonPath("$.available").value(true));
	}

	@Test
	void rejectsInvalidEmailQueryParameter() throws Exception {
		mockMvc.perform(get("/api/v1/auth/email/check-duplicate")
				.param("email", "not-an-email"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void issuesVerificationCode() throws Exception {
		mockMvc.perform(post("/api/v1/auth/email/verification-code")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com"}
					"""))
			.andExpect(status().isOk());

		then(authService).should()
			.issueSignupVerificationCode(new EmailVerificationCodeCommand("user@example.com"));
	}

	@Test
	void rejectsBlankVerificationEmail() throws Exception {
		mockMvc.perform(post("/api/v1/auth/email/verification-code")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":" "}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
	}

	@Test
	void verifiesEmailCode() throws Exception {
		mockMvc.perform(post("/api/v1/auth/email/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","code":"123456"}
					"""))
			.andExpect(status().isOk());

		then(authService).should()
			.verifySignupEmail(new EmailVerifyCommand("user@example.com", "123456"));
	}

	@Test
	void rejectsBlankVerificationCode() throws Exception {
		mockMvc.perform(post("/api/v1/auth/email/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","code":" "}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void signsUpVerifiedUser() throws Exception {
		mockMvc.perform(post("/api/v1/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","password":"secret","name":"Soeun"}
					"""))
			.andExpect(status().isCreated());

		then(authService).should()
			.signup(new SignupCommand("user@example.com", "secret", "Soeun"));
	}

	@Test
	void rejectsSignupWithoutName() throws Exception {
		mockMvc.perform(post("/api/v1/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","password":"secret"}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void logsInAndReturnsBearerToken() throws Exception {
		given(authService.login(any(LoginCommand.class)))
			.willReturn(new LoginResult("access-token"));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","password":"secret"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value("access-token"))
			.andExpect(jsonPath("$.tokenType").value("Bearer"));
	}
}
