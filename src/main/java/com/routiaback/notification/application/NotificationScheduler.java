package com.routiaback.notification.application;

import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.domain.DailyRoutine;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {
    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final DailyRoutineRepositoryPort routines;
    private final NotificationDeliveryService delivery;
    private final Clock clock;

    public NotificationScheduler(DailyRoutineRepositoryPort routines,
            NotificationDeliveryService delivery, Clock clock) {
        this.routines = routines;
        this.delivery = delivery;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${routia.notification.scheduler-delay-ms:60000}")
    public void sendDue() {
        for (DailyRoutine routine : routines.findReadyDueForNotification(clock.instant())) {
            try {
                delivery.deliver(routine);
            } catch (RuntimeException exception) {
                log.warn("Notification processing failed. routineId={} error={}",
                        routine.id(), exception.getClass().getSimpleName());
            }
        }
    }
}
