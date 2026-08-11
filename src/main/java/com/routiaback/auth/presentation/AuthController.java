package com.routiaback.auth.presentation;

import com.routiaback.auth.application.AuthService;
import com.routiaback.auth.application.command.EmailVerificationCodeCommand;
import com.routiaback.auth.application.command.EmailVerifyCommand;
import com.routiaback.auth.application.command.LoginCommand;
import com.routiaback.auth.application.command.SignupCommand;
import com.routiaback.auth.application.result.EmailDuplicateCheckResult;
import com.routiaback.auth.application.result.LoginResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/email/check-duplicate")
	public EmailDuplicateCheckResponse checkDuplicate(@RequestParam @NotBlank @Email String email) {
		EmailDuplicateCheckResult result = authService.checkDuplicate(email);
		return new EmailDuplicateCheckResponse(result.duplicated(), !result.duplicated());
	}

	@PostMapping("/email/verification-code")
	public void issueVerificationCode(@Valid @RequestBody EmailVerificationCodeRequest request) {
		authService.issueSignupVerificationCode(new EmailVerificationCodeCommand(request.email()));
	}

	@PostMapping("/email/verify")
	public void verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
		authService.verifySignupEmail(new EmailVerifyCommand(request.email(), request.code()));
	}

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public void signup(@Valid @RequestBody SignupRequest request) {
		authService.signup(new SignupCommand(request.email(), request.password(), request.name()));
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = authService.login(new LoginCommand(request.email(), request.password()));
		return new LoginResponse(result.accessToken(), result.tokenType());
	}

	public record EmailDuplicateCheckResponse(boolean duplicated, boolean available) {
	}

	public record EmailVerificationCodeRequest(@NotBlank @Email String email) {
	}

	public record EmailVerifyRequest(@NotBlank @Email String email, @NotBlank String code) {
	}

	public record SignupRequest(@NotBlank @Email String email, @NotBlank String password, @NotBlank String name) {
	}

	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
	}

	public record LoginResponse(String accessToken, String tokenType) {
	}
}
