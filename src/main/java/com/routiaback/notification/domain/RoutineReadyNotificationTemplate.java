package com.routiaback.notification.domain;

import java.time.LocalDate;
import java.util.Map;

public final class RoutineReadyNotificationTemplate {
    public static final String TITLE = "오늘의 루틴이 준비됐어요";
    public static final String BODY = "오늘의 맞춤 루틴을 확인해보세요.";

    private RoutineReadyNotificationTemplate() {
    }

    public static Map<String, String> data(Long routineId, LocalDate routineDate) {
        return Map.of(
                "type", NotificationType.DAILY_ROUTINE_READY.name(),
                "routineId", routineId.toString(),
                "routineDate", routineDate.toString()
        );
    }
}
