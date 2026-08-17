package com.routiaback.notification.infrastructure;

import com.routiaback.notification.application.port.PushDeviceRepositoryPort;
import com.routiaback.notification.domain.PushDevice;
import com.routiaback.notification.domain.PushPlatform;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PushDevicePersistenceAdapter implements PushDeviceRepositoryPort {
    private final PushDeviceJpaRepository repository;

    public PushDevicePersistenceAdapter(PushDeviceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PushDevice> findByInstallationId(String installationId) {
        return repository.findByInstallationId(installationId).map(PushDeviceJpaEntity::toDomain);
    }

    @Override
    public Optional<PushDevice> findByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId).map(PushDeviceJpaEntity::toDomain);
    }

    @Override
    public List<PushDevice> findAllActiveByUserId(Long userId) {
        return repository.findAllByUserIdAndActiveTrueAndPlatform(userId, PushPlatform.WEB).stream()
                .map(PushDeviceJpaEntity::toDomain)
                .toList();
    }

    @Override
    public PushDevice save(PushDevice device) {
        return repository.save(PushDeviceJpaEntity.from(device)).toDomain();
    }
}
