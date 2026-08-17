package com.routiaback.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JwtAuthenticationFilterTest {

	@ParameterizedTest
	@ValueSource(strings = {
		"/swagger-ui.html",
		"/swagger-ui/index.html",
		"/v3/api-docs",
		"/v3/api-docs/swagger-config"
	})
	void skipsJwtParsingForSwaggerResources(String path) throws ServletException, IOException {
		JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
		request.addHeader("Authorization", "Bearer invalid-token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isSameAs(request);
		verifyNoInteractions(tokenProvider);
	}
}
