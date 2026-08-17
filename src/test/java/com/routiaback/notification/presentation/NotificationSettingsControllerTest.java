package com.routiaback.notification.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.global.error.GlobalExceptionHandler;
import com.routiaback.global.security.JwtTokenProvider;
import com.routiaback.notification.application.NotificationSettingsService;
import java.time.LocalTime;
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

@WebMvcTest(NotificationSettingsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class NotificationSettingsControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean NotificationSettingsService service;
    @MockitoBean JwtTokenProvider tokenProvider;

    @BeforeEach void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void returnsCurrentSettings() throws Exception {
        given(service.get(1L, 1L)).willReturn(
                new NotificationSettingsService.SettingsResult(false, LocalTime.of(8, 0)));
        mockMvc.perform(get("/api/v1/users/1/notification-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationEnabled").value(false))
                .andExpect(jsonPath("$.data.notificationTime").value("08:00"));
    }

    @Test
    void updatesSettingsAndRejectsMalformedTime() throws Exception {
        given(service.update(any(), any(), any(Boolean.class), any(LocalTime.class))).willReturn(
                new NotificationSettingsService.SettingsResult(true, LocalTime.of(8, 0)));
        mockMvc.perform(patch("/api/v1/users/1/notification-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notificationEnabled\":true,\"notificationTime\":\"08:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationEnabled").value(true));

        mockMvc.perform(patch("/api/v1/users/1/notification-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notificationEnabled\":true,\"notificationTime\":\"24:00\"}"))
                .andExpect(status().isBadRequest());
    }
}
