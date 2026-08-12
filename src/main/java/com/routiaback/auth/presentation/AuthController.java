package com.routiaback.auth.presentation;

import com.routiaback.auth.application.AuthService;
import com.routiaback.auth.application.command.EmailVerificationCodeCommand;
import com.routiaback.auth.application.command.EmailVerifyCommand;
import com.routiaback.auth.application.command.LoginCommand;
import com.routiaback.auth.application.command.SignupCommand;
import com.routiaback.auth.application.result.EmailDuplicateCheckResult;
import com.routiaback.auth.application.result.LoginResult;
import com.routiaback.global.common.validation.NormalizedEmail;
import com.routiaback.global.error.ErrorResponse;
import com.routiaback.global.logging.SensitiveLogSanitizer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Auth", description = "이메일 인증, 회원가입, 로그인 API")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/email/check-duplicate")
	@Operation(
		summary = "이메일 중복 확인",
		description = "앞뒤 공백과 대소문자를 정규화한 뒤 users.email 기준으로 가입 가능 여부를 확인합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "중복 확인 성공",
			content = @Content(schema = @Schema(implementation = EmailDuplicateCheckResponse.class))
		),
		@ApiResponse(
			responseCode = "400",
			description = "이메일 형식 오류",
			content = @Content(
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(name = "INVALID_EMAIL_FORMAT", value = AuthOpenApiExamples.INVALID_EMAIL)
			)
		)
	})
	public EmailDuplicateCheckResponse checkDuplicate(
		@Parameter(description = "중복 확인할 이메일", example = "user@example.com", required = true)
		@RequestParam @NotBlank @NormalizedEmail String email
	) {
		EmailDuplicateCheckResult result = authService.checkDuplicate(email);
		return new EmailDuplicateCheckResponse(result.duplicated(), !result.duplicated());
	}

	@PostMapping("/email/verification-code")
	@Operation(
		summary = "이메일 인증번호 발급",
		description = "6자리 인증번호를 생성하고 hash만 DB에 5분 유효기간으로 저장한 뒤 HTML 메일로 발송합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "인증번호 발급 및 메일 발송 성공"),
		@ApiResponse(
			responseCode = "400",
			description = "이메일 형식 오류",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples =
				@ExampleObject(name = "INVALID_EMAIL_FORMAT", value = AuthOpenApiExamples.INVALID_EMAIL))
		),
		@ApiResponse(
			responseCode = "500",
			description = "SMTP 발송 실패",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples =
				@ExampleObject(name = "EMAIL_SEND_FAILED", value = AuthOpenApiExamples.EMAIL_SEND_FAILED))
		)
	})
	public void issueVerificationCode(@Valid @RequestBody EmailVerificationCodeRequest request) {
		String maskedEmail = SensitiveLogSanitizer.maskEmail(request.email());
		log.info("Email verification request received. email={}", maskedEmail);
		authService.issueSignupVerificationCode(new EmailVerificationCodeCommand(request.email()));
		log.info("Email verification request completed. email={} status=success", maskedEmail);
	}

	@PostMapping("/email/verify")
	@Operation(
		summary = "이메일 인증번호 검증",
		description = "해당 이메일의 가장 최근 SIGNUP 인증정보를 조회해 만료, 소비 여부와 code hash를 검증합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
		@ApiResponse(
			responseCode = "400",
			description = "인증정보 없음, 코드 불일치 또는 만료",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
				@ExampleObject(name = "EMAIL_VERIFICATION_NOT_FOUND", value = AuthOpenApiExamples.VERIFICATION_NOT_FOUND),
				@ExampleObject(name = "EMAIL_VERIFICATION_CODE_MISMATCH", value = AuthOpenApiExamples.CODE_MISMATCH),
				@ExampleObject(name = "EMAIL_VERIFICATION_EXPIRED", value = AuthOpenApiExamples.VERIFICATION_EXPIRED)
			})
		)
	})
	public void verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
		authService.verifySignupEmail(new EmailVerifyCommand(request.email(), request.code()));
	}

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "회원가입",
		description = "검증 완료된 최신 이메일 인증정보를 소비하고 ACTIVE 사용자를 생성합니다. 비밀번호는 BCrypt hash로 저장합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "회원가입 성공"),
		@ApiResponse(
			responseCode = "400",
			description = "이메일 미인증 또는 인증 만료",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
				@ExampleObject(name = "EMAIL_NOT_VERIFIED", value = AuthOpenApiExamples.EMAIL_NOT_VERIFIED),
				@ExampleObject(name = "EMAIL_VERIFICATION_EXPIRED", value = AuthOpenApiExamples.VERIFICATION_EXPIRED)
			})
		),
		@ApiResponse(
			responseCode = "409",
			description = "이미 사용 중인 이메일",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples =
				@ExampleObject(name = "EMAIL_ALREADY_EXISTS", value = AuthOpenApiExamples.EMAIL_ALREADY_EXISTS))
		)
	})
	public void signup(@Valid @RequestBody SignupRequest request) {
		authService.signup(new SignupCommand(request.email(), request.password(), request.name()));
	}

	@PostMapping("/login")
	@Operation(
		summary = "로그인",
		description = "이메일과 비밀번호 및 계정 상태를 검증하고 JWT Access Token을 발급하며 last_login_at을 갱신합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "로그인 성공",
			content = @Content(schema = @Schema(implementation = LoginResponse.class))
		),
		@ApiResponse(
			responseCode = "401",
			description = "이메일 또는 비밀번호 불일치",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples =
				@ExampleObject(name = "INVALID_CREDENTIALS", value = AuthOpenApiExamples.INVALID_CREDENTIALS))
		),
		@ApiResponse(
			responseCode = "403",
			description = "차단 또는 탈퇴 계정",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
				@ExampleObject(name = "ACCOUNT_BLOCKED", value = AuthOpenApiExamples.ACCOUNT_BLOCKED),
				@ExampleObject(name = "ACCOUNT_WITHDRAWN", value = AuthOpenApiExamples.ACCOUNT_WITHDRAWN)
			})
		)
	})
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = authService.login(new LoginCommand(request.email(), request.password()));
		return new LoginResponse(result.accessToken(), result.tokenType());
	}

	@Schema(name = "EmailDuplicateCheckResponse", description = "이메일 중복 확인 결과")
	public record EmailDuplicateCheckResponse(
		@Schema(description = "이미 가입된 이메일인지 여부", example = "false") boolean duplicated,
		@Schema(description = "가입에 사용할 수 있는 이메일인지 여부", example = "true") boolean available
	) {
	}

	@Schema(name = "EmailVerificationCodeRequest", description = "이메일 인증번호 발급 요청")
	public record EmailVerificationCodeRequest(
		@NotBlank @NormalizedEmail
		@Schema(description = "인증번호를 받을 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
		String email
	) {
	}

	@Schema(name = "EmailVerifyRequest", description = "이메일 인증번호 검증 요청")
	public record EmailVerifyRequest(
		@NotBlank @NormalizedEmail
		@Schema(description = "인증번호를 발급받은 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
		String email,
		@NotBlank
		@Schema(description = "메일로 수신한 6자리 인증번호", example = "123456", minLength = 6, maxLength = 6, pattern = "^[0-9]{6}$", requiredMode = Schema.RequiredMode.REQUIRED)
		String code
	) {
	}

	@Schema(name = "SignupRequest", description = "회원가입 요청")
	public record SignupRequest(
		@NotBlank @NormalizedEmail
		@Schema(description = "인증 완료된 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
		String email,
		@NotBlank
		@Schema(description = "로그인 비밀번호. 현재 길이 정책은 미확정", example = "routia-password", writeOnly = true, requiredMode = Schema.RequiredMode.REQUIRED)
		String password,
		@NotBlank
		@Schema(description = "사용자 이름", example = "김루티", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
		String name
	) {
	}

	@Schema(name = "LoginRequest", description = "로그인 요청")
	public record LoginRequest(
		@NotBlank @NormalizedEmail
		@Schema(description = "가입한 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
		String email,
		@NotBlank
		@Schema(description = "가입 시 등록한 비밀번호", example = "routia-password", writeOnly = true, requiredMode = Schema.RequiredMode.REQUIRED)
		String password
	) {
	}

	@Schema(name = "LoginResponse", description = "로그인 성공 응답")
	public record LoginResponse(
		@Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.signature")
		String accessToken,
		@Schema(description = "Authorization scheme", example = "Bearer")
		String tokenType
	) {
	}
}
