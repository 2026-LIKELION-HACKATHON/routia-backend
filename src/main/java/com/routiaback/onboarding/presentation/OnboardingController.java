package com.routiaback.onboarding.presentation;

import com.routiaback.global.common.apiResponse.ApiResponse;
import com.routiaback.global.error.ErrorResponse;
import com.routiaback.onboarding.application.OnboardingService;
import com.routiaback.onboarding.application.command.Step1Command;
import com.routiaback.onboarding.application.command.Step2Command;
import com.routiaback.onboarding.application.command.Step3Command;
import com.routiaback.onboarding.domain.OnboardingProgress;
import com.routiaback.onboarding.domain.OnboardingStatus;
import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.Gender;
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
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
@Tag(name = "Onboarding", description = "사용자 데이터 기반 단계별 온보딩 API")
@SecurityRequirement(name = "bearerAuth")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/step1")
    @Operation(summary = "온보딩 1단계 저장",
            description = "신체 프로필과 목표·고민을 저장합니다. 재제출 시 최종 값으로 갱신합니다.")
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
            description = "피부 유형과 피부 고민을 저장합니다. 1단계 완료 후 요청할 수 있습니다.")
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
            description = "루틴 선호와 알림 시각을 저장합니다. 2단계 완료 후 요청할 수 있습니다.")
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
            description = "1~3단계 완료 후 오늘의 AI 루틴을 즉시 생성합니다. 최초 루틴은 알림 발송 대상이 아닙니다.")
    @OnboardingApiResponses
    public ApiResponse<ProgressResponse> complete(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(ProgressResponse.from(onboardingService.complete(userId)));
    }

    @Schema(name = "OnboardingStep1Request")
    public record Step1Request(
            @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 4, fraction = 1)
            BigDecimal height,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 4, fraction = 1)
            BigDecimal weight,
            @NotNull Gender gender,
            @NotNull AgeGroup ageGroup,
            @NotNull List<@NotBlank @Size(max = 30) String> bodyConcerns,
            @NotNull BodyGoal bodyGoal
    ) {
        Step1Command toCommand() {
            return new Step1Command(height, weight, gender, ageGroup, bodyConcerns, bodyGoal);
        }
    }

    @Schema(name = "OnboardingStep2Request",
            description = "skinConcerns는 빈 배열을 허용하며 전달된 최종 목록으로 전체 교체합니다.")
    public record Step2Request(
            @NotNull SkinType skinType,
            @NotNull List<@NotBlank @Size(max = 30) String> skinConcerns
    ) {
        Step2Command toCommand() {
            return new Step2Command(skinType, skinConcerns);
        }
    }

    @Schema(name = "OnboardingStep3Request")
    public record Step3Request(
            @NotNull RoutineTimePreference routineTimePreference,
            @NotNull RoutineDifficulty routineDifficulty,
            @NotNull @Pattern(regexp = "(?:[01]\\d|2[0-3]):[0-5]\\d")
            @Schema(type = "string", example = "23:59")
            String notificationTime
    ) {
        Step3Command toCommand() {
            return new Step3Command(routineTimePreference, routineDifficulty, LocalTime.parse(notificationTime));
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
}
