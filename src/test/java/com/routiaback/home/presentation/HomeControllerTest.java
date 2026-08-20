package com.routiaback.home.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.global.error.GlobalExceptionHandler;
import com.routiaback.global.security.JwtTokenProvider;
import com.routiaback.home.application.HomeService;
import com.routiaback.home.application.TodayDirectionService;
import com.routiaback.home.application.result.TodayDirectionResult;
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

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HomeService homeService;

    @MockitoBean
    private TodayDirectionService todayDirectionService;

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
    void returnsTodayDirectionModalData() throws Exception {
        given(todayDirectionService.getToday(1L)).willReturn(new TodayDirectionResult(
                10L,
                LocalDate.of(2026, 8, 20),
                "😎",
                "오늘은 자외선이 강한 날씨예요!",
                "아침 보습과 자외선 차단에 신경 쓰는 것이 좋아요.",
                List.of(
                        new TodayDirectionResult.Section(
                                "MORNING", "오전", "☀️",
                                List.of(new TodayDirectionResult.Item(
                                        101L, "MORNING", "자외선 차단제 바르기",
                                        "외출 전에 얼굴과 목에 고르게 바르세요."))),
                        new TodayDirectionResult.Section("AFTERNOON", "오후", "🌇", List.of()),
                        new TodayDirectionResult.Section("NIGHT", "밤", "🌙", List.of()))));

        mockMvc.perform(get("/api/v1/home/today-direction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routineId").value(10))
                .andExpect(jsonPath("$.data.date").value("2026-08-20"))
                .andExpect(jsonPath("$.data.emoji").value("😎"))
                .andExpect(jsonPath("$.data.title").value("오늘은 자외선이 강한 날씨예요!"))
                .andExpect(jsonPath("$.data.description").value("아침 보습과 자외선 차단에 신경 쓰는 것이 좋아요."))
                .andExpect(jsonPath("$.data.sections[0].period").value("MORNING"))
                .andExpect(jsonPath("$.data.sections[0].label").value("오전"))
                .andExpect(jsonPath("$.data.sections[0].icon").value("☀️"))
                .andExpect(jsonPath("$.data.sections[0].items[0].itemId").value(101))
                .andExpect(jsonPath("$.data.sections[0].items[0].timeSlot").value("MORNING"))
                .andExpect(jsonPath("$.data.sections[2].period").value("NIGHT"));
    }
}
