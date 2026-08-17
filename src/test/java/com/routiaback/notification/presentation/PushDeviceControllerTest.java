package com.routiaback.notification.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.global.error.GlobalExceptionHandler;
import com.routiaback.global.security.JwtTokenProvider;
import com.routiaback.notification.application.PushDeviceService;
import com.routiaback.notification.domain.PushPlatform;
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

@WebMvcTest(PushDeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PushDeviceControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean PushDeviceService service;
    @MockitoBean JwtTokenProvider tokenProvider;

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registersWebDeviceWithoutReturningToken() throws Exception {
        given(service.register(1L, 1L, "web-token", PushPlatform.WEB)).willReturn(
                new PushDeviceService.DeviceResult(10L, PushPlatform.WEB, true,
                        Instant.parse("2026-08-17T00:00:00Z")));

        mockMvc.perform(post("/api/v1/users/1/push-devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"web-token\",\"platform\":\"WEB\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.token").doesNotExist());
    }

    @Test
    void rejectsBlankTokenAndUnsupportedPlatform() throws Exception {
        mockMvc.perform(post("/api/v1/users/1/push-devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\" \",\"platform\":\"WEB\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/users/1/push-devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token\",\"platform\":\"IOS\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivatesDevice() throws Exception {
        given(service.deactivate(1L, 1L, 10L)).willReturn(
                new PushDeviceService.DeviceResult(10L, PushPlatform.WEB, false,
                        Instant.parse("2026-08-17T00:00:00Z")));

        mockMvc.perform(delete("/api/v1/users/1/push-devices/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
        then(service).should().deactivate(1L, 1L, 10L);
    }
}
