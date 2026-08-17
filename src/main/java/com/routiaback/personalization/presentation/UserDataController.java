package com.routiaback.personalization.presentation;

import com.routiaback.global.common.apiResponse.ApiResponse;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.global.error.ErrorResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/{id}")
@Tag(name = "유조 데이터", description = "사용자 프로필과 개인화 니즈 API")
@SecurityRequirement(name = "bearerAuth")
public class UserDataController {

    private final PersonalizationService personalizationService;

    public UserDataController(PersonalizationService personalizationService) {
        this.personalizationService = personalizationService;
    }

    @GetMapping("/profile")
    @Operation(summary = "프로필 정보 조회", description = "JWT 사용자와 경로 사용자가 같은 경우에만 프로필을 조회합니다.")
    @UserDataApiResponses
    public ApiResponse<ProfileResponse> getProfile(
            @AuthenticationPrincipal Long authenticatedUserId,
            @PathVariable("id") Long userId
    ) {
        return ApiResponse.success(ProfileResponse.from(
                personalizationService.getProfile(authenticatedUserId, userId)));
    }

    @PatchMapping("/profile")
    @Operation(summary = "프로필 정보 수정", description = "전달한 필드만 수정하며 위치 변경 시 위치 출처와 변경 시각을 함께 저장합니다.")
    @UserDataApiResponses
    public ApiResponse<ProfileResponse> updateProfile(
            @AuthenticationPrincipal Long authenticatedUserId,
            @PathVariable("id") Long userId,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        ProfileResult result = personalizationService.updateProfile(authenticatedUserId, userId, request.toCommand());
        return ApiResponse.success(ProfileResponse.from(result));
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 업로드", description = "file 필드로 JPEG, PNG 또는 WebP 이미지를 업로드합니다. 최대 크기는 5MB입니다.")
    @UserDataApiResponses
    public ApiResponse<ProfileResponse> uploadPhoto(
            @AuthenticationPrincipal Long authenticatedUserId,
            @PathVariable("id") Long userId,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            ProfileResult result = personalizationService.uploadProfileImage(
                    authenticatedUserId, userId, new ProfileImageUpload(file.getBytes(), file.getContentType()));
            return ApiResponse.success(ProfileResponse.from(result));
        } catch (IOException exception) {
            throw new ApiException(ErrorCode.PROFILE_IMAGE_STORAGE_FAILED, exception);
        }
    }

    @GetMapping("/needs")
    @Operation(summary = "프로필 니즈 정보 조회", description = "신체·피부 고민과 루틴 선호 정보를 조회합니다.")
    @UserDataApiResponses
    public ApiResponse<NeedsResponse> getNeeds(
            @AuthenticationPrincipal Long authenticatedUserId,
            @PathVariable("id") Long userId
    ) {
        return ApiResponse.success(NeedsResponse.from(
                personalizationService.getNeeds(authenticatedUserId, userId)));
    }

    @PatchMapping("/needs")
    @Operation(summary = "프로필 니즈 정보 수정", description = "전달한 값만 수정합니다. 고민 목록은 merge하지 않고 최종 목록으로 전체 교체합니다.")
    @UserDataApiResponses
    public ApiResponse<NeedsResponse> updateNeeds(
            @AuthenticationPrincipal Long authenticatedUserId,
            @PathVariable("id") Long userId,
            @Valid @RequestBody NeedsUpdateRequest request
    ) {
        return ApiResponse.success(NeedsResponse.from(
                personalizationService.updateNeeds(authenticatedUserId, userId, request.toCommand())));
    }

    @Schema(name = "ProfileUpdateRequest", description = "null 또는 생략한 값은 기존 값을 유지합니다.")
    public record ProfileUpdateRequest(
            @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 4, fraction = 1)
            BigDecimal height,
            @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 4, fraction = 1)
            BigDecimal weight,
            Gender gender,
            AgeGroup ageGroup,
            @Size(max = 50) String regionSido,
            @Size(max = 50) String regionSigungu,
            @DecimalMin("-90.0") @DecimalMax("90.0") @Digits(integer = 3, fraction = 7)
            BigDecimal latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") @Digits(integer = 3, fraction = 7)
            BigDecimal longitude,
            LocationSource locationSource
    ) {
        UpdateProfileCommand toCommand() {
            return new UpdateProfileCommand(height, weight, gender, ageGroup, regionSido, regionSigungu,
                    latitude, longitude, locationSource);
        }
    }

    @Schema(name = "NeedsUpdateRequest", description = "고민 목록은 전달 시 전체 교체되며 빈 배열로 모두 제거할 수 있습니다.")
    public record NeedsUpdateRequest(
            BodyGoal bodyGoal,
            @Size(max = 3) List<@jakarta.validation.constraints.NotNull BodyGoal> bodyGoals,
            List<@NotBlank @Size(max = 30) String> bodyConcerns,
            SkinType skinType,
            List<@NotBlank @Size(max = 30) String> skinConcerns,
            @Size(max = 4) List<@NotBlank @Size(max = 40) String> ownedTools,
            RoutineTimePreference routineTimePreference,
            RoutineDifficulty routineDifficulty
    ) {
        UpdateNeedsCommand toCommand() {
            return new UpdateNeedsCommand(bodyGoal, bodyConcerns, skinType, skinConcerns,
                    routineTimePreference, routineDifficulty, bodyGoals, ownedTools);
        }
    }

    @Schema(name = "ProfileResponse")
    public record ProfileResponse(
            String userName,
            BigDecimal height,
            BigDecimal weight,
            Gender gender,
            AgeGroup ageGroup,
            @Schema(description = "저장된 프로필 이미지 객체 key")
            String profileImage,
            String regionSido,
            String regionSigungu,
            BigDecimal latitude,
            BigDecimal longitude,
            LocationSource locationSource,
            Instant locationUpdatedAt
    ) {
        static ProfileResponse from(ProfileResult result) {
            return new ProfileResponse(result.userName(), result.height(), result.weight(), result.gender(), result.ageGroup(),
                    result.profileImage(), result.regionSido(), result.regionSigungu(), result.latitude(),
                    result.longitude(), result.locationSource(), result.locationUpdatedAt());
        }
    }

    @Schema(name = "NeedsResponse")
    public record NeedsResponse(
            BodyGoal bodyGoal,
            List<BodyGoal> bodyGoals,
            List<String> bodyConcerns,
            SkinType skinType,
            List<String> skinConcerns,
            List<String> ownedTools,
            RoutineTimePreference routineTimePreference,
            RoutineDifficulty routineDifficulty
    ) {
        static NeedsResponse from(NeedsResult result) {
            return new NeedsResponse(result.bodyGoal(), result.bodyGoals(), result.bodyConcerns(), result.skinType(),
                    result.skinConcerns(), result.ownedTools(), result.routineTimePreference(), result.routineDifficulty());
        }
    }

    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자 정보 접근"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "이미지 저장 등 서버 처리 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    private @interface UserDataApiResponses {
    }
}
