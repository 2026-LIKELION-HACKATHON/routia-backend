package com.routiaback.notification.presentation;

import com.routiaback.global.common.apiResponse.ApiResponse;
import com.routiaback.global.error.ErrorResponse;
import com.routiaback.notification.application.PushDeviceService;
import com.routiaback.notification.domain.PushPlatform;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{id}/push-devices")
@Tag(name = "Push Device", description = "Web FCM 기기 토큰 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class PushDeviceController {
    private final PushDeviceService service;

    public PushDeviceController(PushDeviceService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Web Push 기기 등록",
            description = "브라우저가 발급받은 FCM registration token을 등록합니다. 같은 token은 재활성화되며 현재 사용자에게 소유권이 이전됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 또는 재활성화 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 token 또는 platform",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자 접근")
    })
    public ApiResponse<DeviceResponse> register(@AuthenticationPrincipal Long authenticatedUserId,
            @PathVariable("id") Long userId, @Valid @RequestBody RegisterDeviceRequest request) {
        return ApiResponse.success(DeviceResponse.from(service.register(authenticatedUserId, userId,
                request.token(), request.platform())));
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Web Push 기기 비활성화",
            description = "기기 row는 보존하고 isActive=false로 변경해 이후 발송 대상에서 제외합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비활성화 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자 접근"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기기 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ApiResponse<DeviceResponse> deactivate(@AuthenticationPrincipal Long authenticatedUserId,
            @PathVariable("id") Long userId, @PathVariable Long deviceId) {
        return ApiResponse.success(DeviceResponse.from(
                service.deactivate(authenticatedUserId, userId, deviceId)));
    }

    public record RegisterDeviceRequest(
            @NotBlank @Size(max = 512)
            @Schema(description = "브라우저 Firebase Messaging에서 발급받은 registration token",
                    example = "FCM_WEB_REGISTRATION_TOKEN", writeOnly = true)
            String token,
            @NotNull
            @Schema(description = "웹 플랫폼", example = "WEB")
            PushPlatform platform
    ) {
    }

    public record DeviceResponse(Long id, PushPlatform platform, boolean active, Instant lastSeenAt) {
        static DeviceResponse from(PushDeviceService.DeviceResult result) {
            return new DeviceResponse(result.id(), result.platform(), result.active(), result.lastSeenAt());
        }
    }
}
