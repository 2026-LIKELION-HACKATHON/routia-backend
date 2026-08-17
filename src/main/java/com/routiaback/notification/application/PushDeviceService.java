package com.routiaback.notification.application;

import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.notification.application.port.PushDeviceRepositoryPort;
import com.routiaback.notification.domain.PushDevice;
import com.routiaback.notification.domain.PushPlatform;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushDeviceService {
    private static final int MAX_INSTALLATION_ID_LENGTH = 255;

    private final UserRepositoryPort users;
    private final PushDeviceRepositoryPort devices;
    private final Clock clock;

    public PushDeviceService(UserRepositoryPort users, PushDeviceRepositoryPort devices, Clock clock) {
        this.users = users;
        this.devices = devices;
        this.clock = clock;
    }

    @Transactional
    public DeviceResult register(Long authenticatedUserId, Long userId, String installationId,
            PushPlatform platform) {
        validateUser(authenticatedUserId, userId);
        String normalizedInstallationId = installationId == null ? null : installationId.trim();
        if (normalizedInstallationId == null || normalizedInstallationId.isEmpty()
                || normalizedInstallationId.length() > MAX_INSTALLATION_ID_LENGTH
                || platform != PushPlatform.WEB) {
            throw new ApiException(ErrorCode.INVALID_PUSH_DEVICE);
        }
        Instant now = clock.instant();
        PushDevice device = devices.findByInstallationId(normalizedInstallationId)
                .map(existing -> existing.claim(userId, platform, now))
                .orElseGet(() -> PushDevice.register(userId, normalizedInstallationId, platform, now));
        return DeviceResult.from(devices.save(device));
    }

    @Transactional
    public DeviceResult deactivate(Long authenticatedUserId, Long userId, Long deviceId) {
        validateUser(authenticatedUserId, userId);
        PushDevice device = devices.findByIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.PUSH_DEVICE_NOT_FOUND));
        return DeviceResult.from(devices.save(device.deactivate(clock.instant())));
    }

    private void validateUser(Long authenticatedUserId, Long userId) {
        if (!Objects.equals(authenticatedUserId, userId)) {
            throw new ApiException(ErrorCode.USER_DATA_ACCESS_DENIED);
        }
        users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND))
                .validateLoginAllowed();
    }

    public record DeviceResult(Long id, PushPlatform platform, boolean active, Instant lastSeenAt) {
        static DeviceResult from(PushDevice device) {
            return new DeviceResult(device.id(), device.platform(), device.active(), device.lastSeenAt());
        }
    }
}
