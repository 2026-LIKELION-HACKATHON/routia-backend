package com.routiaback.global.error;

import java.util.List;

public record ErrorResponse(String code, String message, List<FieldErrorResponse> fieldErrors) {

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.name(), errorCode.message(), List.of());
	}

	public record FieldErrorResponse(String field, String message) {
	}
}
