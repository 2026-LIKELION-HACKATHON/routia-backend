package com.routiaback.notification.infrastructure;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

interface PushDeviceJpaRepository extends JpaRepository<PushDeviceJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PushDeviceJpaEntity> findByToken(String token);
    Optional<PushDeviceJpaEntity> findByIdAndUserId(Long id, Long userId);
    List<PushDeviceJpaEntity> findAllByUserIdAndActiveTrue(Long userId);
}
