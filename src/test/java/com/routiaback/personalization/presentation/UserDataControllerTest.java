package com.routiaback.personalization.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.routiaback.global.error.GlobalExceptionHandler;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.global.security.JwtTokenProvider;
import com.routiaback.personalization.application.PersonalizationService;
import com.routiaback.personalization.application.command.ProfileImageUpload;
import com.routiaback.personalization.application.command.UpdateNeedsCommand;
import com.routiaback.personalization.application.command.UpdateProfileCommand;
import com.routiaback.personalization.application.result.NeedsResult;
import com.routiaback.personalization.application.result.ProfileResult;
import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.LocationSource;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import java.math.BigDecimal;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserDataController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonalizationService personalizationService;

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
    void getsOwnProfileUsingApiResponseContract() throws Exception {
        given(personalizationService.getProfile(1L, 1L)).willReturn(profileResult());

        mockMvc.perform(get("/api/v1/users/1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.height").value(165.5))
                .andExpect(jsonPath("$.data.gender").value("FEMALE"))
                .andExpect(jsonPath("$.data.locationSource").value("GPS"));
    }

    @Test
    void rejectsAnotherUsersPathAsForbidden() throws Exception {
        given(personalizationService.getProfile(1L, 2L))
                .willThrow(new ApiException(ErrorCode.USER_DATA_ACCESS_DENIED));

        mockMvc.perform(get("/api/v1/users/2/profile"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_DATA_ACCESS_DENIED"));
    }

    @Test
    void patchesProfileWithPathAndAuthenticatedUserIds() throws Exception {
        given(personalizationService.updateProfile(any(), any(), any())).willReturn(profileResult());

        mockMvc.perform(patch("/api/v1/users/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "height": 165.5,
                                  "gender": "FEMALE",
                                  "latitude": 37.5665000,
                                  "longitude": 126.9780000,
                                  "locationSource": "GPS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.height").value(165.5));

        then(personalizationService).should().updateProfile(any(Long.class), any(Long.class),
                any(UpdateProfileCommand.class));
    }

    @Test
    void rejectsUnknownEnumAsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"gender":"UNKNOWN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void returnsNeedsAndEmptyConcernArrays() throws Exception {
        given(personalizationService.getNeeds(1L, 1L)).willReturn(new NeedsResult(
                BodyGoal.MAINTAIN, List.of(), SkinType.DRY, List.of(),
                RoutineTimePreference.MORNING, RoutineDifficulty.SIMPLE));

        mockMvc.perform(get("/api/v1/users/1/needs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bodyConcerns").isArray())
                .andExpect(jsonPath("$.data.bodyConcerns").isEmpty())
                .andExpect(jsonPath("$.data.routineDifficulty").value("SIMPLE"));
    }

    @Test
    void patchesNeedsWithFinalConcernLists() throws Exception {
        given(personalizationService.updateNeeds(any(), any(), any())).willReturn(new NeedsResult(
                BodyGoal.FAT_LOSS, List.of("SWELLING"), SkinType.OILY, List.of("PORE"),
                RoutineTimePreference.EVENING, RoutineDifficulty.MINIMAL));

        mockMvc.perform(patch("/api/v1/users/1/needs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bodyGoal": "FAT_LOSS",
                                  "bodyConcerns": ["SWELLING"],
                                  "skinType": "OILY",
                                  "skinConcerns": ["PORE"],
                                  "routineTimePreference": "EVENING",
                                  "routineDifficulty": "MINIMAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bodyConcerns[0]").value("SWELLING"));

        then(personalizationService).should().updateNeeds(any(Long.class), any(Long.class),
                any(UpdateNeedsCommand.class));
    }

    @Test
    void uploadsMultipartProfilePhoto() throws Exception {
        given(personalizationService.uploadProfileImage(any(), any(), any())).willReturn(profileResult());
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/users/1/photo")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileImage").value("1/profile.jpg"));

        then(personalizationService).should().uploadProfileImage(any(Long.class), any(Long.class),
                any(ProfileImageUpload.class));
    }

    private ProfileResult profileResult() {
        return new ProfileResult(new BigDecimal("165.5"), new BigDecimal("55.2"), Gender.FEMALE,
                AgeGroup.TWENTIES, "1/profile.jpg", "서울특별시", "중구",
                new BigDecimal("37.5665000"), new BigDecimal("126.9780000"), LocationSource.GPS,
                Instant.parse("2026-08-15T00:00:00Z"));
    }
}
