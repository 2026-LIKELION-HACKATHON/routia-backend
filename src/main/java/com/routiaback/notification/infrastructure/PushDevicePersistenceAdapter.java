package com.routiaback.notification.infrastructure;

import com.routiaback.notification.application.port.PushDeviceRepositoryPort;
import com.routiaback.notification.domain.PushDevice;
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
    public Optional<PushDevice> findByToken(String token) {
        return repository.findByToken(token).map(PushDeviceJpaEntity::toDomain);
    }

    @Override
    public Optional<PushDevice> findByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId).map(PushDeviceJpaEntity::toDomain);
    }

    @Override
    public List<PushDevice> findAllActiveByUserId(Long userId) {
        return repository.findAllByUserIdAndActiveTrue(userId).stream()
                .map(PushDeviceJpaEntity::toDomain)
                .toList();
    }

    @Override
    public PushDevice save(PushDevice device) {
        return repository.save(PushDeviceJpaEntity.from(device)).toDomain();
    }
}
