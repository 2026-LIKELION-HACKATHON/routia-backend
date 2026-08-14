package com.routiaback.personalization.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.global.error.GlobalExceptionHandler;
import com.routiaback.global.security.JwtAuthenticationFilter;
import com.routiaback.global.security.JwtTokenProvider;
import com.routiaback.global.security.SecurityConfig;
import com.routiaback.personalization.application.PersonalizationService;
import com.routiaback.personalization.application.result.ProfileResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserDataController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class UserDataSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonalizationService personalizationService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @Test
    void rejectsProfileRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/1/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsProfileRequestWithInvalidToken() throws Exception {
        given(tokenProvider.parseUserId("invalid"))
                .willThrow(new IllegalArgumentException("invalid token"));

        mockMvc.perform(get("/api/v1/users/1/profile")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passesJwtSubjectToProtectedUserDataEndpoint() throws Exception {
        given(tokenProvider.parseUserId("valid")).willReturn(1L);
        given(personalizationService.getProfile(1L, 1L)).willReturn(new ProfileResult(
                null, null, null, null, null, null, null, null, null, null, null));

        mockMvc.perform(get("/api/v1/users/1/profile")
                        .header("Authorization", "Bearer valid"))
                .andExpect(status().isOk());
    }
}
