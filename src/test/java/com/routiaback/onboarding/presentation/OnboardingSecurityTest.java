package com.routiaback.onboarding.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.global.error.GlobalExceptionHandler;
import com.routiaback.global.security.JwtAuthenticationFilter;
import com.routiaback.global.security.JwtTokenProvider;
import com.routiaback.global.security.SecurityConfig;
import com.routiaback.onboarding.application.OnboardingService;
import com.routiaback.onboarding.domain.OnboardingProgress;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OnboardingController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class OnboardingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OnboardingService onboardingService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @Test
    void rejectsProgressRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/progress"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passesJwtSubjectToOnboardingEndpoint() throws Exception {
        given(tokenProvider.parseUserId("valid")).willReturn(1L);
        given(onboardingService.getProgress(1L)).willReturn(
                OnboardingProgress.notStarted(1L, Instant.parse("2026-08-15T00:00:00Z")));

        mockMvc.perform(get("/api/v1/onboarding/progress")
                        .header("Authorization", "Bearer valid"))
                .andExpect(status().isOk());
    }
}
