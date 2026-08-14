package com.routiaback.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "이메일 인증 정보를 찾을 수 없습니다."),
	EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다."),
	EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
	EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 필요합니다."),
	EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
	ACCOUNT_BLOCKED(HttpStatus.FORBIDDEN, "차단된 계정입니다."),
	ACCOUNT_WITHDRAWN(HttpStatus.FORBIDDEN, "탈퇴한 계정입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	USER_DATA_ACCESS_DENIED(HttpStatus.FORBIDDEN, "다른 사용자의 정보에 접근할 수 없습니다."),
	INVALID_PROFILE_DATA(HttpStatus.BAD_REQUEST, "프로필 정보가 올바르지 않습니다."),
	INVALID_BODY_CONCERN(HttpStatus.BAD_REQUEST, "존재하지 않거나 사용할 수 없는 신체 고민입니다."),
	INVALID_SKIN_CONCERN(HttpStatus.BAD_REQUEST, "존재하지 않거나 사용할 수 없는 피부 고민입니다."),
	INVALID_PROFILE_IMAGE(HttpStatus.BAD_REQUEST, "지원하지 않는 프로필 이미지 형식입니다."),
	PROFILE_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "프로필 이미지는 5MB 이하여야 합니다."),
	PROFILE_IMAGE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 이미지 저장에 실패했습니다."),
	ONBOARDING_STEP_ORDER_INVALID(HttpStatus.CONFLICT, "이전 온보딩 단계를 먼저 완료해야 합니다."),
	ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND, "오늘의 루틴을 찾을 수 없습니다."),
	USER_LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 위치 정보를 찾을 수 없습니다."),
	ROUTINE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "할 일 항목을 찾을 수 없습니다."),
	ROUTINE_ITEM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 루틴 항목만 체크할 수 있습니다.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	public HttpStatus status() {
		return status;
	}

	public String message() {
		return message;
	}
}
