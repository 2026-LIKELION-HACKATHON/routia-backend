package com.routiaback.notification.infrastructure;

import jakarta.persistence.LockModeType;
import com.routiaback.notification.domain.PushPlatform;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

interface PushDeviceJpaRepository extends JpaRepository<PushDeviceJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PushDeviceJpaEntity> findByInstallationId(String installationId);
    Optional<PushDeviceJpaEntity> findByIdAndUserId(Long id, Long userId);
    List<PushDeviceJpaEntity> findAllByUserIdAndActiveTrueAndPlatform(Long userId, PushPlatform platform);
}
