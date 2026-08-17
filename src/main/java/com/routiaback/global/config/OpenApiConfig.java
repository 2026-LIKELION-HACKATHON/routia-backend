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
				.description("Routia 백엔드 API 문서입니다.")
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
