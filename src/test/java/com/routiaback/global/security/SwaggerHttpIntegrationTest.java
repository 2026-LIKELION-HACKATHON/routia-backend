package com.routiaback.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
	"spring.datasource.url=jdbc:h2:mem:routia-swagger;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"routia.jwt.secret=swagger-http-test-secret-at-least-32-characters",
	"routia.ai.provider=unconfigured",
	"routia.notification.provider=unconfigured"
})
class SwaggerHttpIntegrationTest {

	private final HttpClient httpClient = HttpClient.newBuilder()
		.followRedirects(HttpClient.Redirect.NEVER)
		.build();

	@Autowired
	private ApplicationContext applicationContext;

	@LocalServerPort
	private int port;

	@Test
	void servesApiDocsAndSwaggerUiWithoutAuthentication() throws Exception {
		String baseUrl = "http://localhost:" + port;
		HttpResponse<String> apiDocs = get(baseUrl + "/v3/api-docs");
		assertThat(apiDocs.statusCode()).isEqualTo(200);
		assertThat(apiDocs.body()).contains("\"openapi\"");

		HttpResponse<String> swaggerEntry = get(baseUrl + "/swagger-ui.html");
		assertThat(swaggerEntry.statusCode()).isBetween(300, 399);
		String location = swaggerEntry.headers().firstValue("Location").orElseThrow();

		HttpResponse<String> swaggerUi = get(URI.create(baseUrl).resolve(location).toString());
		assertThat(swaggerUi.statusCode()).isEqualTo(200);
		assertThat(swaggerUi.body()).containsIgnoringCase("swagger ui");

		assertThat(applicationContext.getBeansOfType(InMemoryUserDetailsManager.class)).isEmpty();
	}

	private HttpResponse<String> get(String url) throws Exception {
		return httpClient.send(
			HttpRequest.newBuilder(URI.create(url)).GET().build(),
			HttpResponse.BodyHandlers.ofString());
	}
}
