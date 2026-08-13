package com.routiaback.routine.presentation;

import com.routiaback.global.common.apiResponse.ApiResponse;
import com.routiaback.routine.application.RoutineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "루틴", description = "오늘의 루틴 관련 API")
@RestController
@RequestMapping("/api/v1/routines")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RoutineController {

    private final RoutineService routineService;

    @Operation(summary = "오늘 할 일 전체 조회")
    @GetMapping("/today")
    public ApiResponse<RoutineResponse> getToday(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(RoutineResponse.from(routineService.getToday(userId)));
    }
}