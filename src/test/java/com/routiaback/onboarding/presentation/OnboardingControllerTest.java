package com.routiaback.onboarding.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.global.error.GlobalExceptionHandler;
import com.routiaback.global.security.JwtTokenProvider;
import com.routiaback.onboarding.application.OnboardingService;
import com.routiaback.onboarding.application.command.Step1Command;
import com.routiaback.onboarding.application.command.Step2Command;
import com.routiaback.onboarding.application.command.Step3Command;
import com.routiaback.onboarding.domain.OnboardingProgress;
import com.routiaback.routine.application.generation.GeneratedRoutine;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OnboardingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class OnboardingControllerTest {

    private static final Instant NOW = Instant.parse("2026-08-15T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OnboardingService onboardingService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void completesStep1WithCurrentUserAndReturnsProgress() throws Exception {
        given(onboardingService.completeStep1(any(), any())).willReturn(step1());

        mockMvc.perform(post("/api/v1/onboarding/step1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "height": 165.5,
                                  "weight": 55.2,
                                  "gender": "FEMALE",
                                  "ageGroup": "TWENTIES",
                                  "bodyConcerns": ["SWELLING", "FATIGUE"],
                                  "bodyGoal": "MAINTAIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.lastCompletedStep").value(1));

        then(onboardingService).should().completeStep1(any(Long.class), any(Step1Command.class));
    }

    @Test
    void completesStep2WithEmptyConcernArray() throws Exception {
        given(onboardingService.completeStep2(any(), any())).willReturn(step2());

        mockMvc.perform(post("/api/v1/onboarding/step2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skinType":"DRY","skinConcerns":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastCompletedStep").value(2));

        then(onboardingService).should().completeStep2(any(Long.class), any(Step2Command.class));
    }

    @Test
    void completesStep3AtTimeBoundaries() throws Exception {
        given(onboardingService.completeStep3(any(), any())).willReturn(step3());

        mockMvc.perform(post("/api/v1/onboarding/step3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "routineTimePreference":"EVENING",
                                  "routineDifficulty":"MINIMAL",
                                  "notificationTime":"23:59"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastCompletedStep").value(3));

        then(onboardingService).should().completeStep3(any(Long.class), any(Step3Command.class));
    }

    @Test
    void returnsPersistedProgressForCurrentUser() throws Exception {
        given(onboardingService.getProgress(1L)).willReturn(step2());

        mockMvc.perform(get("/api/v1/onboarding/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.lastCompletedStep").value(2))
                .andExpect(jsonPath("$.data.step1CompletedAt").exists())
                .andExpect(jsonPath("$.data.step2CompletedAt").exists());
    }

    @Test
    void rejectsUnknownEnumsMalformedNumbersAndInvalidTime() throws Exception {
        mockMvc.perform(post("/api/v1/onboarding/step1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "height":"not-a-number",
                                  "weight":55.2,
                                  "gender":"UNKNOWN",
                                  "ageGroup":"TWENTIES",
                                  "bodyConcerns":[],
                                  "bodyGoal":"MAINTAIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/v1/onboarding/step3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "routineTimePreference":"MORNING",
                                  "routineDifficulty":"SIMPLE",
                                  "notificationTime":"24:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void rejectsMissingRequiredFieldsAndTooManyDecimalPlaces() throws Exception {
        mockMvc.perform(post("/api/v1/onboarding/step1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "height":165.55,
                                  "weight":55.2,
                                  "gender":"FEMALE",
                                  "ageGroup":"TWENTIES",
                                  "bodyGoal":"MAINTAIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void completesOnboardingAndReturnsGeneratedAiRoutine() throws Exception {
        OnboardingProgress completed = step3().startGenerating(NOW.plusSeconds(180)).complete(NOW.plusSeconds(181));
        GeneratedRoutine routine = new GeneratedRoutine("오늘의 방향", "홈 코멘트", List.of(
                new GeneratedRoutine.GeneratedItem(
                        "MORNING", "SKIN", "미온수 세안", "부드럽게 세안하세요.", "CLEAN", "피부 청결")));
        given(onboardingService.complete(1L)).willReturn(
                new OnboardingService.CompleteResult(completed, 10L, routine));

        mockMvc.perform(post("/api/v1/onboarding/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.lastCompletedStep").value(3))
                .andExpect(jsonPath("$.data.routine.routineId").value(10))
                .andExpect(jsonPath("$.data.routine.directionText").value("오늘의 방향"))
                .andExpect(jsonPath("$.data.routine.homeComment").value("홈 코멘트"))
                .andExpect(jsonPath("$.data.routine.items[0].timeSlot").value("MORNING"))
                .andExpect(jsonPath("$.data.routine.items[0].category").value("SKIN"))
                .andExpect(jsonPath("$.data.routine.items[0].title").value("미온수 세안"))
                .andExpect(jsonPath("$.data.routine.items[0].detail").value("부드럽게 세안하세요."))
                .andExpect(jsonPath("$.data.routine.items[0].effectCode").value("CLEAN"))
                .andExpect(jsonPath("$.data.routine.items[0].expectedEffect").value("피부 청결"));

        then(onboardingService).should().complete(1L);
    }

    private OnboardingProgress step1() {
        return OnboardingProgress.notStarted(1L, NOW).completeStep1(NOW);
    }

    private OnboardingProgress step2() {
        return step1().completeStep2(NOW.plusSeconds(60));
    }

    private OnboardingProgress step3() {
        return step2().completeStep3(NOW.plusSeconds(120));
    }
}
