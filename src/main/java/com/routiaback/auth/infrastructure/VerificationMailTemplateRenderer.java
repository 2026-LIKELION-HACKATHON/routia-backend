package com.routiaback.auth.infrastructure;

import com.routiaback.auth.application.port.VerificationMailRendererPort;
import org.springframework.stereotype.Component;

@Component
public class VerificationMailTemplateRenderer implements VerificationMailRendererPort {

	@Override
	public String render(String code) {
		return """
			<!doctype html>
			<html lang="ko">
			<body>
			  <h1>Routia</h1>
			  <p>이메일 인증번호를 확인해 주세요.</p>
			  <p>인증번호</p>
			  <strong style="font-size: 24px;">%s</strong>
			  <p>인증번호는 5분 동안 유효합니다.</p>
			  <p>본인이 요청하지 않은 경우 이 메일을 무시해 주세요.</p>
			</body>
			</html>
			""".formatted(code);
	}
}
