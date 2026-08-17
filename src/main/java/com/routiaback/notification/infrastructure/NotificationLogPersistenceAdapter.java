package com.routiaback.notification.infrastructure;

import com.routiaback.notification.application.port.NotificationLogRepositoryPort;
import com.routiaback.notification.domain.NotificationLog;
import com.routiaback.notification.domain.NotificationType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationLogPersistenceAdapter implements NotificationLogRepositoryPort {
    private final NotificationLogJpaRepository repository;

    public NotificationLogPersistenceAdapter(NotificationLogJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean reserve(Long routineId, Long pushDeviceId, NotificationType type, Instant now) {
        return repository.reserve(routineId, pushDeviceId, type.name(), now) == 1;
    }

    @Override
    public Optional<NotificationLog> find(Long routineId, Long pushDeviceId, NotificationType type) {
        return repository.findByRoutineIdAndPushDeviceIdAndType(routineId, pushDeviceId, type)
                .map(NotificationLogJpaEntity::toDomain);
    }

    @Override
    public NotificationLog save(NotificationLog log) {
        return repository.save(NotificationLogJpaEntity.from(log)).toDomain();
    }
}
