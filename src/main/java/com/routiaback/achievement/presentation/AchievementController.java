package com.routiaback.achievement.presentation;

import com.routiaback.achievement.application.AchievementService;
import com.routiaback.global.common.apiResponse.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "성취도", description = "성취도 및 변화 분석 API")
@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "성취도 요약 조회")
    @GetMapping("/summary")
    public ApiResponse<AchievementSummaryResponse> getSummary(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(AchievementSummaryResponse.from(achievementService.getSummary(userId)));
    }
}