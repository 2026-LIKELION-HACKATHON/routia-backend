package com.routiaback.global.error;

import com.routiaback.global.common.validation.NormalizedEmail;
import com.routiaback.global.logging.SensitiveLogSanitizer;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
		if (exception.getErrorCode().status().is5xxServerError()) {
			Throwable rootCause = SensitiveLogSanitizer.rootCause(exception);
			log.error(
				"ApiException converted to HTTP response. status={} errorCode={} exceptionClass={} message={} causeClass={} rootCauseClass={} rootCauseMessage={} stackTrace=\n{}",
				exception.getErrorCode().status().value(),
				exception.getErrorCode().name(),
				exception.getClass().getName(),
				SensitiveLogSanitizer.sanitize(exception.getMessage()),
				exception.getCause() == null ? "<none>" : exception.getCause().getClass().getName(),
				rootCause.getClass().getName(),
				SensitiveLogSanitizer.sanitize(rootCause.getMessage()),
				SensitiveLogSanitizer.stackTrace(exception)
			);
		} else {
			log.info(
				"ApiException converted to HTTP response. status={} errorCode={} exceptionClass={}",
				exception.getErrorCode().status().value(),
				exception.getErrorCode().name(),
				exception.getClass().getName()
			);
		}
		return ResponseEntity.status(exception.getErrorCode().status())
			.body(ErrorResponse.of(exception.getErrorCode()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
		List<ErrorResponse.FieldErrorResponse> fields = exception.getBindingResult().getFieldErrors().stream()
			.map(error -> new ErrorResponse.FieldErrorResponse(error.getField(), error.getDefaultMessage()))
			.toList();
		if (exception.getBindingResult().getFieldErrors().stream()
			.anyMatch(error -> "NormalizedEmail".equals(error.getCode()))) {
			return invalidEmail(fields);
		}
		return ResponseEntity.badRequest()
			.body(new ErrorResponse("INVALID_REQUEST", "요청 값이 올바르지 않습니다.", fields));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
		List<ErrorResponse.FieldErrorResponse> fields = exception.getConstraintViolations().stream()
			.map(violation -> new ErrorResponse.FieldErrorResponse(
				violation.getPropertyPath().toString(),
				violation.getMessage()
			))
			.toList();
		if (exception.getConstraintViolations().stream()
			.anyMatch(violation -> violation.getConstraintDescriptor().getAnnotation() instanceof NormalizedEmail)) {
			return invalidEmail(fields);
		}
		return ResponseEntity.badRequest()
			.body(new ErrorResponse("INVALID_REQUEST", "요청 값이 올바르지 않습니다.", fields));
	}

	private ResponseEntity<ErrorResponse> invalidEmail(List<ErrorResponse.FieldErrorResponse> fields) {
		return ResponseEntity.badRequest().body(new ErrorResponse(
			ErrorCode.INVALID_EMAIL_FORMAT.name(),
			ErrorCode.INVALID_EMAIL_FORMAT.message(),
			fields
		));
	}
}
