package com.routiaback.global.error;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
		return ResponseEntity.status(exception.getErrorCode().status())
			.body(ErrorResponse.of(exception.getErrorCode()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
		List<ErrorResponse.FieldErrorResponse> fields = exception.getBindingResult().getFieldErrors().stream()
			.map(error -> new ErrorResponse.FieldErrorResponse(error.getField(), error.getDefaultMessage()))
			.toList();
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
		return ResponseEntity.badRequest()
			.body(new ErrorResponse("INVALID_REQUEST", "요청 값이 올바르지 않습니다.", fields));
	}
}
