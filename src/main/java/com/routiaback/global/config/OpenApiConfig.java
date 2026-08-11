package com.routiaback.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI routiaOpenApi() {
		return new OpenAPI()
			.info(new Info()
				.title("Routia API")
				.version("v1")
				.description("""
					Routia 백엔드 API 문서입니다.

					Auth 테스트 순서:
					1. 이메일 중복 확인
					2. 인증번호 발급 및 메일 수신
					3. 인증번호 검증
					4. 회원가입
					5. 로그인 후 Access Token 확인

					로그인 후 우측 상단 Authorize에 `Bearer` 접두어 없이 Access Token만 입력합니다.
					""")
				.contact(new Contact().name("Routia Backend Team")))
			.components(new Components().addSecuritySchemes(
				"bearerAuth",
				new SecurityScheme()
					.name("bearerAuth")
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.description("로그인 응답의 accessToken 값을 입력합니다.")
			));
	}
}
