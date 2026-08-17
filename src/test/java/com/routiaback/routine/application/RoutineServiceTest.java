package com.routiaback.routine.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.application.port.RoutineItemRepositoryPort;
import com.routiaback.routine.application.result.RoutineTodayResult;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineItem;
import com.routiaback.routine.domain.RoutineStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RoutineServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-17T00:00:00Z");

    @Test
    void returnsStoredRoutineDescriptionsAndChecklistDetails() {
        LocalDate today = LocalDate.now();
        DailyRoutineRepositoryPort routines = mock(DailyRoutineRepositoryPort.class);
        RoutineItemRepositoryPort items = mock(RoutineItemRepositoryPort.class);
        RoutineService service = new RoutineService(Clock.fixed(NOW, ZoneOffset.UTC), routines, items);
        DailyRoutine routine = new DailyRoutine(
                10L, 1L, 20L, today, RoutineStatus.READY,
                "오늘의 방향", "이 방향을 선택한 이유",
                RoutineDifficulty.SIMPLE, RoutineTimePreference.MORNING,
                "{}", "{}", null, "gpt-5.6-luna", "routine-v2",
                NOW, null, NOW, NOW);
        RoutineItem completedItem = new RoutineItem(
                11L, 10L, "MORNING", "SKIN", "자외선 차단제 바르기",
                "외출 전에 얼굴과 목에 고르게 바르세요.", "UV_PROTECTION",
                "자외선 노출을 줄이는 데 도움을 줄 수 있습니다.",
                1, true, NOW, NOW, NOW);
        given(routines.findByUserIdAndRoutineDate(1L, today)).willReturn(Optional.of(routine));
        given(items.findAllByRoutineIdOrderBySortOrder(10L)).willReturn(List.of(completedItem));

        RoutineTodayResult result = service.getToday(1L);

        assertThat(result.routineId()).isEqualTo(10L);
        assertThat(result.directionText()).isEqualTo("오늘의 방향");
        assertThat(result.homeComment()).isEqualTo("이 방향을 선택한 이유");
        assertThat(result.completedCount()).isEqualTo(1);
        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.itemId()).isEqualTo(11L);
            assertThat(item.timeSlot()).isEqualTo("MORNING");
            assertThat(item.category()).isEqualTo("SKIN");
            assertThat(item.detail()).contains("외출 전에");
            assertThat(item.effectCode()).isEqualTo("UV_PROTECTION");
            assertThat(item.expectedEffect()).contains("도움");
            assertThat(item.sortOrder()).isEqualTo(1);
            assertThat(item.completed()).isTrue();
            assertThat(item.completedAt()).isEqualTo(NOW);
        });
    }
}
