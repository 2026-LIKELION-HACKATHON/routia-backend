package com.routiaback.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.global.security.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("prod")
@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:routia-prod-profile;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"routia.jwt.secret=production-profile-test-secret-at-least-32-characters",
	"routia.cors.allowed-origins=https://frontend.example",
	"routia.ai.provider=unconfigured",
	"routia.notification.provider=unconfigured"
})
@AutoConfigureMockMvc
class ProductionProfileContextTest {

	@Autowired
	private CorsConfig corsConfig;

	@Autowired
	private SecurityFilterChain securityFilterChain;

	@Autowired
	private HealthEndpoint healthEndpoint;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void startsProductionProfileWithCorsSecurityAndHealth() throws Exception {
		assertThat(corsConfig).isNotNull();
		assertThat(securityFilterChain).isNotNull();
		assertThat(healthEndpoint).isNotNull();

		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
	}
}
