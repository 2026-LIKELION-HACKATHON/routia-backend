package com.routiaback.notification.presentation;

import com.routiaback.global.common.apiResponse.ApiResponse;
import com.routiaback.notification.application.NotificationSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalTime;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{id}/notification-settings")
@Tag(name = "Notification Settings", description = "My Page 알림 설정 API")
@SecurityRequirement(name = "bearerAuth")
public class NotificationSettingsController {
    private final NotificationSettingsService service;
    public NotificationSettingsController(NotificationSettingsService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "알림 설정 조회", description = "알림 OFF 상태에서도 일일 루틴 생성은 계속됩니다.")
    public ApiResponse<SettingsResponse> get(@AuthenticationPrincipal Long authId, @PathVariable("id") Long userId) {
        return ApiResponse.success(SettingsResponse.from(service.get(authId, userId)));
    }

    @PatchMapping
    @Operation(summary = "알림 설정 수정", description = "알림 ON 시 시각이 필요하며 OFF 시 기존 시각을 보존합니다.")
    public ApiResponse<SettingsResponse> update(@AuthenticationPrincipal Long authId,
            @PathVariable("id") Long userId, @Valid @RequestBody SettingsRequest request) {
        LocalTime time = request.notificationTime() == null ? null : LocalTime.parse(request.notificationTime());
        return ApiResponse.success(SettingsResponse.from(
                service.update(authId, userId, request.notificationEnabled(), time)));
    }

    public record SettingsRequest(
            @NotNull Boolean notificationEnabled,
            @Pattern(regexp = "(?:[01]\\d|2[0-3]):[0-5]\\d") String notificationTime) { }

    public record SettingsResponse(boolean notificationEnabled,
            @Schema(type = "string", example = "08:00") String notificationTime) {
        static SettingsResponse from(NotificationSettingsService.SettingsResult result) {
            return new SettingsResponse(result.notificationEnabled(),
                    result.notificationTime() == null ? null : result.notificationTime().toString());
        }
    }
}
