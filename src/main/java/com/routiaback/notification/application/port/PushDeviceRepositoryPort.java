package com.routiaback.notification.application.port;

import com.routiaback.notification.domain.PushDevice;
import java.util.List;
import java.util.Optional;

public interface PushDeviceRepositoryPort {
    Optional<PushDevice> findByInstallationId(String installationId);
    Optional<PushDevice> findByIdAndUserId(Long id, Long userId);
    List<PushDevice> findAllActiveByUserId(Long userId);
    PushDevice save(PushDevice device);
}
