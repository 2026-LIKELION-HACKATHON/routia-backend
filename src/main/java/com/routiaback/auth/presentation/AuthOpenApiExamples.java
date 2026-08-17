package com.routiaback.auth.presentation;

final class AuthOpenApiExamples {

	static final String INVALID_EMAIL = """
		{"code":"INVALID_EMAIL_FORMAT","message":"이메일 형식이 올바르지 않습니다.","fieldErrors":[{"field":"email","message":"이메일 형식이 올바르지 않습니다."}]}
		""";
	static final String EMAIL_SEND_FAILED = """
		{"code":"EMAIL_SEND_FAILED","message":"이메일 발송에 실패했습니다.","fieldErrors":[]}
		""";
	static final String VERIFICATION_NOT_FOUND = """
		{"code":"EMAIL_VERIFICATION_NOT_FOUND","message":"이메일 인증 정보를 찾을 수 없습니다.","fieldErrors":[]}
		""";
	static final String CODE_MISMATCH = """
		{"code":"EMAIL_VERIFICATION_CODE_MISMATCH","message":"인증번호가 올바르지 않습니다.","fieldErrors":[]}
		""";
	static final String VERIFICATION_EXPIRED = """
		{"code":"EMAIL_VERIFICATION_EXPIRED","message":"인증번호가 만료되었습니다.","fieldErrors":[]}
		""";
	static final String EMAIL_NOT_VERIFIED = """
		{"code":"EMAIL_NOT_VERIFIED","message":"이메일 인증이 필요합니다.","fieldErrors":[]}
		""";
	static final String PASSWORD_CONFIRMATION_MISMATCH = """
		{"code":"PASSWORD_CONFIRMATION_MISMATCH","message":"비밀번호와 비밀번호 확인이 일치하지 않습니다.","fieldErrors":[]}
		""";
	static final String EMAIL_ALREADY_EXISTS = """
		{"code":"EMAIL_ALREADY_EXISTS","message":"이미 사용 중인 이메일입니다.","fieldErrors":[]}
		""";
	static final String INVALID_CREDENTIALS = """
		{"code":"INVALID_CREDENTIALS","message":"이메일 또는 비밀번호가 올바르지 않습니다.","fieldErrors":[]}
		""";
	static final String ACCOUNT_BLOCKED = """
		{"code":"ACCOUNT_BLOCKED","message":"차단된 계정입니다.","fieldErrors":[]}
		""";
	static final String ACCOUNT_WITHDRAWN = """
		{"code":"ACCOUNT_WITHDRAWN","message":"탈퇴한 계정입니다.","fieldErrors":[]}
		""";

	private AuthOpenApiExamples() {
	}
}
