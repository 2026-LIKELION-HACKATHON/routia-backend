package com.routiaback.notification.infrastructure;

import com.routiaback.notification.domain.NotificationType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface NotificationLogJpaRepository extends JpaRepository<NotificationLogJpaEntity, Long> {
    Optional<NotificationLogJpaEntity> findByRoutineIdAndPushDeviceIdAndType(
            Long routineId, Long pushDeviceId, NotificationType type);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO notification_logs
                (routine_id, push_device_id, notification_type, status, created_at, updated_at)
            VALUES (:routineId, :deviceId, :type, 'PENDING', :now, :now)
            """, nativeQuery = true)
    int reserve(@Param("routineId") Long routineId, @Param("deviceId") Long deviceId,
            @Param("type") String type, @Param("now") Instant now);
}
