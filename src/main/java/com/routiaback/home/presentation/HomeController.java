package com.routiaback.home.presentation;

import com.routiaback.global.common.apiResponse.ApiResponse;
import com.routiaback.home.application.HomeService;
import com.routiaback.home.application.result.HomeResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈", description = "홈 화면 관련 API")
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class HomeController {

    private final HomeService homeService;

    @Operation(summary = "홈 화면 조회")
    @GetMapping
    public ApiResponse<HomeResponse> getHome(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(HomeResponse.from(homeService.getHome(userId)));
    }
}