package com.routiaback.routine.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.global.error.GlobalExceptionHandler;
import com.routiaback.global.security.JwtTokenProvider;
import com.routiaback.routine.application.RoutineService;
import com.routiaback.routine.application.result.RoutineTodayResult;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RoutineController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoutineControllerTest {

    private static final Instant COMPLETED_AT = Instant.parse("2026-08-17T01:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoutineService routineService;

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
    void returnsAdditiveRoutineDetailsForChecklistAndDirectionUx() throws Exception {
        given(routineService.getToday(1L)).willReturn(new RoutineTodayResult(
                10L,
                LocalDate.of(2026, 8, 17),
                "오늘의 방향",
                "오늘 이 방향을 선택한 이유",
                1,
                1,
                List.of(new RoutineTodayResult.Item(
                        11L, "MORNING", "SKIN", "자외선 차단제 바르기",
                        "외출 전에 얼굴과 목에 고르게 바르세요.", "UV_PROTECTION",
                        "자외선 노출을 줄이는 데 도움을 줄 수 있습니다.",
                        1, true, COMPLETED_AT))));

        mockMvc.perform(get("/api/v1/routines/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.routineId").value(10))
                .andExpect(jsonPath("$.data.date").value("2026-08-17"))
                .andExpect(jsonPath("$.data.directionText").value("오늘의 방향"))
                .andExpect(jsonPath("$.data.homeComment").value("오늘 이 방향을 선택한 이유"))
                .andExpect(jsonPath("$.data.completedCount").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.items[0].itemId").value(11))
                .andExpect(jsonPath("$.data.items[0].timeSlot").value("MORNING"))
                .andExpect(jsonPath("$.data.items[0].category").value("SKIN"))
                .andExpect(jsonPath("$.data.items[0].title").value("자외선 차단제 바르기"))
                .andExpect(jsonPath("$.data.items[0].detail").value("외출 전에 얼굴과 목에 고르게 바르세요."))
                .andExpect(jsonPath("$.data.items[0].effectCode").value("UV_PROTECTION"))
                .andExpect(jsonPath("$.data.items[0].expectedEffect").value("자외선 노출을 줄이는 데 도움을 줄 수 있습니다."))
                .andExpect(jsonPath("$.data.items[0].sortOrder").value(1))
                .andExpect(jsonPath("$.data.items[0].completed").value(true))
                .andExpect(jsonPath("$.data.items[0].completedAt").value("2026-08-17T01:00:00Z"));
    }
}
