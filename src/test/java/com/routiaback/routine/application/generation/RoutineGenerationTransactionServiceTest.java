package com.routiaback.routine.application.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.application.port.RoutineItemRepositoryPort;
import com.routiaback.routine.application.port.WeatherSnapshotRepositoryPort;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RoutineGenerationTransactionServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-16T00:00:00Z");

    @Test
    void reservesFailedRoutineForRetryInsteadOfReturningItUnchanged() {
        DailyRoutineRepositoryPort routines = mock(DailyRoutineRepositoryPort.class);
        RoutineGenerationTransactionService service = new RoutineGenerationTransactionService(
                routines,
                mock(RoutineItemRepositoryPort.class),
                mock(WeatherSnapshotRepositoryPort.class));
        DailyRoutine failed = DailyRoutine.generating(
                        1L,
                        LocalDate.of(2026, 8, 16),
                        RoutineDifficulty.SIMPLE,
                        RoutineTimePreference.MORNING,
                        null,
                        NOW.minusSeconds(60))
                .failed("ROUTINE_GENERATION_FAILED", NOW.minusSeconds(30));
        given(routines.findByUserIdAndRoutineDate(1L, LocalDate.of(2026, 8, 16)))
                .willReturn(Optional.of(failed));
        given(routines.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        RoutineGenerationTransactionService.Reservation result = service.reserve(
                1L,
                LocalDate.of(2026, 8, 16),
                RoutineDifficulty.COMPLEX,
                RoutineTimePreference.ANY,
                null,
                NOW);

        assertThat(result.created()).isTrue();
        assertThat(result.routine().status()).isEqualTo(RoutineStatus.GENERATING);
        assertThat(result.routine().generationErrorCode()).isNull();
        assertThat(result.routine().difficultySnapshot()).isEqualTo(RoutineDifficulty.COMPLEX);
        ArgumentCaptor<DailyRoutine> saved = ArgumentCaptor.forClass(DailyRoutine.class);
        verify(routines).save(saved.capture());
        assertThat(saved.getValue().id()).isEqualTo(failed.id());
    }
}
