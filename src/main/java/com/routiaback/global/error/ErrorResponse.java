package com.routiaback.global.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "ErrorResponse", description = "공통 API 오류 응답")
public record ErrorResponse(
	@Schema(description = "애플리케이션 오류 코드", example = "EMAIL_NOT_VERIFIED")
	String code,
	@Schema(description = "사용자에게 표시 가능한 오류 메시지", example = "이메일 인증이 필요합니다.")
	String message,
	@Schema(description = "입력 필드별 검증 오류. 필드 오류가 없으면 빈 배열")
	List<FieldErrorResponse> fieldErrors
) {

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.name(), errorCode.message(), List.of());
	}

	@Schema(name = "FieldErrorResponse", description = "입력 필드 검증 오류")
	public record FieldErrorResponse(
		@Schema(description = "오류가 발생한 필드", example = "email")
		String field,
		@Schema(description = "필드 검증 메시지", example = "이메일 형식이 올바르지 않습니다.")
		String message
	) {
	}
}
