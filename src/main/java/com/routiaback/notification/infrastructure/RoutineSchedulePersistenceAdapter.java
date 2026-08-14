package com.routiaback.notification.infrastructure;

import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.RoutineSchedule;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RoutineSchedulePersistenceAdapter implements RoutineScheduleRepositoryPort {

    private final RoutineScheduleJpaRepository repository;

    public RoutineSchedulePersistenceAdapter(RoutineScheduleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RoutineSchedule> findByUserId(Long userId) {
        return repository.findById(userId).map(RoutineScheduleJpaEntity::toDomain);
    }

    @Override
    public RoutineSchedule save(RoutineSchedule schedule) {
        return repository.save(RoutineScheduleJpaEntity.from(schedule)).toDomain();
    }
}
