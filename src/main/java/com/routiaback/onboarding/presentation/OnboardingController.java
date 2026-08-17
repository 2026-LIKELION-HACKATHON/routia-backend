package com.routiaback.onboarding.presentation;

import com.routiaback.global.common.apiResponse.ApiResponse;
import com.routiaback.global.error.ErrorResponse;
import com.routiaback.onboarding.application.OnboardingService;
import com.routiaback.onboarding.application.command.Step1Command;
import com.routiaback.onboarding.application.command.Step0Command;
import com.routiaback.onboarding.application.command.Step2Command;
import com.routiaback.onboarding.application.command.Step3Command;
import com.routiaback.onboarding.domain.OnboardingProgress;
import com.routiaback.onboarding.domain.OnboardingStatus;
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.routiaback.personalization.application.command.ProfileImageUpload;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/onboarding")
@Tag(name = "온보딩", description = "사용자 데이터 기반 단계별 온보딩 API")
@SecurityRequirement(name = "bearerAuth")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping(value = "/step0", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "온보딩 0단계 저장",
            description = "사용자 이름과 선택 프로필 이미지를 저장합니다. 이미지는 JPEG, PNG, WebP, 최대 5MB입니다.")
    @OnboardingApiResponses
    public ApiResponse<ProgressResponse> completeStep0(
            @AuthenticationPrincipal Long userId,
            @RequestPart("userName") @NotBlank @Size(max = 50) String userName,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            ProfileImageUpload upload = profileImage == null || profileImage.isEmpty() ? null
                    : new ProfileImageUpload(profileImage.getBytes(), profileImage.getContentType());
            return ApiResponse.success(ProgressResponse.from(
                    onboardingService.completeStep0(userId, new Step0Command(userName, upload))));
        } catch (IOException exception) {
            throw new com.routiaback.global.error.ApiException(
                    com.routiaback.global.error.ErrorCode.PROFILE_IMAGE_STORAGE_FAILED, exception);
        }
    }

    @PostMapping("/step1")
    @Operation(summary = "온보딩 1단계 저장",
            description = "신체 프로필과 거주지·좌표를 저장합니다. 0단계 완료 후 요청할 수 있습니다.")
    @OnboardingApiResponses
    public ApiResponse<ProgressResponse> completeStep1(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody Step1Request request
    ) {
        return ApiResponse.success(ProgressResponse.from(
                onboardingService.completeStep1(userId, request.toCommand())));
    }

    @PostMapping("/step2")
    @Operation(summary = "온보딩 2단계 저장",
            description = "피부·신체 고민, 다중 목표, 보유 도구를 저장합니다. 목록은 최종 값으로 교체됩니다.")
    @OnboardingApiResponses
    public ApiResponse<ProgressResponse> completeStep2(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody Step2Request request
    ) {
        return ApiResponse.success(ProgressResponse.from(
                onboardingService.completeStep2(userId, request.toCommand())));
    }

    @PostMapping("/step3")
    @Operation(summary = "온보딩 3단계 저장",
            description = "루틴 시간 선호와 난이도만 저장합니다. 알림은 My Page 알림 설정에서 관리합니다.")
    @OnboardingApiResponses
    public ApiResponse<ProgressResponse> completeStep3(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody Step3Request request
    ) {
        return ApiResponse.success(ProgressResponse.from(
                onboardingService.completeStep3(userId, request.toCommand())));
    }

    @GetMapping("/progress")
    @Operation(summary = "온보딩 진행 상태 조회",
            description = "중간 이탈 후 재진입에 사용할 마지막 완료 단계와 상태를 반환합니다.")
    @OnboardingApiResponses
    public ApiResponse<ProgressResponse> getProgress(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(ProgressResponse.from(onboardingService.getProgress(userId)));
    }

    @PostMapping("/complete")
    @Operation(summary = "온보딩 완료 및 최초 루틴 생성",
            description = "1~3단계 완료 후 오늘의 AI 루틴을 즉시 생성하고 전체 결과를 반환합니다. "
                    + "이미 생성된 경우 AI를 다시 호출하지 않고 저장된 결과를 반환합니다.")
    @OnboardingCompleteApiResponses
    public ApiResponse<CompleteResponse> complete(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(CompleteResponse.from(onboardingService.complete(userId)));
    }

    @Schema(name = "OnboardingStep1Request")
    public record Step1Request(
            @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 4, fraction = 1)
            BigDecimal height,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 4, fraction = 1)
            BigDecimal weight,
            @NotNull Gender gender,
            @NotNull AgeGroup ageGroup,
            @NotBlank @Size(max = 50) String regionSido,
            @NotBlank @Size(max = 50) String regionSigungu,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") @Digits(integer = 3, fraction = 7)
            BigDecimal latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") @Digits(integer = 3, fraction = 7)
            BigDecimal longitude
    ) {
        Step1Command toCommand() {
            return new Step1Command(height, weight, gender, ageGroup, regionSido, regionSigungu,
                    latitude, longitude, LocationSource.MANUAL);
        }
    }

    @Schema(name = "OnboardingStep2Request",
            description = "skinConcerns는 빈 배열을 허용하며 전달된 최종 목록으로 전체 교체합니다.")
    public record Step2Request(
            @NotNull SkinType skinType,
            @NotNull @Size(max = 3) List<@NotBlank @Size(max = 30) String> skinConcerns,
            @NotNull @Size(max = 4) List<@NotBlank @Size(max = 40) String> ownedTools,
            @NotNull @Size(max = 3) List<@NotBlank @Size(max = 30) String> bodyConcerns,
            @NotNull @Size(min = 1, max = 3) List<@NotNull BodyGoal> bodyGoals
    ) {
        Step2Command toCommand() {
            return new Step2Command(skinType, skinConcerns, ownedTools, bodyConcerns, bodyGoals);
        }
    }

    @Schema(name = "OnboardingStep3Request")
    public record Step3Request(
            @NotNull RoutineTimePreference routineTimePreference,
            @NotNull RoutineDifficulty routineDifficulty
    ) {
        Step3Command toCommand() {
            return new Step3Command(routineTimePreference, routineDifficulty);
        }
    }

    @Schema(name = "OnboardingProgressResponse")
    public record ProgressResponse(
            OnboardingStatus status,
            int lastCompletedStep,
            Instant step1CompletedAt,
            Instant step2CompletedAt,
            Instant step3CompletedAt,
            Instant completedAt
    ) {
        static ProgressResponse from(OnboardingProgress progress) {
            return new ProgressResponse(progress.status(), progress.lastCompletedStep(),
                    progress.step1CompletedAt(), progress.step2CompletedAt(),
                    progress.step3CompletedAt(), progress.completedAt());
        }
    }

    @Schema(name = "OnboardingCompleteResponse",
            description = "온보딩 완료 상태와 오늘 생성된 AI 루틴을 함께 반환합니다.")
    public record CompleteResponse(
            OnboardingStatus status,
            int lastCompletedStep,
            Instant step1CompletedAt,
            Instant step2CompletedAt,
            Instant step3CompletedAt,
            Instant completedAt,
            GeneratedRoutineResponse routine
    ) {
        static CompleteResponse from(OnboardingService.CompleteResult result) {
            OnboardingProgress progress = result.progress();
            return new CompleteResponse(progress.status(), progress.lastCompletedStep(),
                    progress.step1CompletedAt(), progress.step2CompletedAt(),
                    progress.step3CompletedAt(), progress.completedAt(),
                    GeneratedRoutineResponse.from(result.routineId(), result.routine()));
        }
    }

    @Schema(name = "OnboardingGeneratedRoutineResponse")
    public record GeneratedRoutineResponse(
            Long routineId,
            String directionText,
            String homeComment,
            List<GeneratedRoutineItemResponse> items
    ) {
        static GeneratedRoutineResponse from(Long routineId,
                com.routiaback.routine.application.generation.GeneratedRoutine routine) {
            return new GeneratedRoutineResponse(routineId, routine.directionText(), routine.homeComment(),
                    routine.items().stream().map(GeneratedRoutineItemResponse::from).toList());
        }
    }

    @Schema(name = "OnboardingGeneratedRoutineItemResponse")
    public record GeneratedRoutineItemResponse(
            String timeSlot,
            String category,
            String title,
            String detail,
            String effectCode,
            String expectedEffect
    ) {
        static GeneratedRoutineItemResponse from(
                com.routiaback.routine.application.generation.GeneratedRoutine.GeneratedItem item) {
            return new GeneratedRoutineItemResponse(item.timeSlot(), item.category(), item.title(),
                    item.detail(), item.effectCode(), item.expectedEffect());
        }
    }

    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "요청 값 또는 코드 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "이전 단계 미완료",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    private @interface OnboardingApiResponses {
    }

    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "온보딩 완료 및 AI 루틴 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "사용자 위치 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "온보딩 1~3단계 미완료",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502", description = "AI 루틴 생성 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503", description = "AI Provider 설정 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    private @interface OnboardingCompleteApiResponses {
    }
}
