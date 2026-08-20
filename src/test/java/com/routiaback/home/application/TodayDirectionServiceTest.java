package com.routiaback.home.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.home.application.result.TodayDirectionResult;
import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.application.port.RoutineItemRepositoryPort;
import com.routiaback.routine.application.port.WeatherSnapshotRepositoryPort;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineItem;
import com.routiaback.routine.domain.RoutineStatus;
import com.routiaback.routine.domain.WeatherSnapshot;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodayDirectionServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 20);
    private static final Instant NOW = Instant.parse("2026-08-19T15:00:00Z");

    @Mock
    private DailyRoutineRepositoryPort dailyRoutineRepository;

    @Mock
    private RoutineItemRepositoryPort routineItemRepository;

    @Mock
    private WeatherSnapshotRepositoryPort weatherSnapshotRepository;

    private TodayDirectionService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new TodayDirectionService(
                clock, dailyRoutineRepository, routineItemRepository, weatherSnapshotRepository);
    }

    @Test
    void groupsRoutineItemsIntoMorningAfternoonAndNight() {
        DailyRoutine routine = routine(10L, 20L);
        given(dailyRoutineRepository.findByUserIdAndRoutineDate(USER_ID, TODAY))
                .willReturn(Optional.of(routine));
        given(routineItemRepository.findAllByRoutineIdOrderBySortOrder(10L))
                .willReturn(List.of(
                        item(101L, "MORNING", "아침 보습", 1),
                        item(102L, "MORNING", "자외선 차단제", 2),
                        item(103L, "AFTERNOON", "수분 섭취", 3),
                        item(104L, "EVENING", "가벼운 운동", 4),
                        item(105L, "BEDTIME", "스크린 타임 줄이기", 5)));
        given(weatherSnapshotRepository.findWeatherById(20L))
                .willReturn(Optional.of(weather("맑음", "25", "8")));

        TodayDirectionResult result = service.getToday(USER_ID);

        assertThat(result.routineId()).isEqualTo(10L);
        assertThat(result.date()).isEqualTo(TODAY);
        assertThat(result.emoji()).isEqualTo("😎");
        assertThat(result.title()).isEqualTo("오늘은 자외선이 강한 날씨예요!");
        assertThat(result.sections()).extracting(TodayDirectionResult.Section::period)
                .containsExactly("MORNING", "AFTERNOON", "NIGHT");
        assertThat(result.sections().get(0).items()).hasSize(2);
        assertThat(result.sections().get(1).items()).hasSize(1);
        assertThat(result.sections().get(2).items())
                .extracting(TodayDirectionResult.Item::timeSlot)
                .containsExactly("EVENING", "BEDTIME");
    }

    @Test
    void returnsStableFallbackEmojiWhenLegacyRoutineHasNoWeatherSnapshot() {
        given(dailyRoutineRepository.findByUserIdAndRoutineDate(USER_ID, TODAY))
                .willReturn(Optional.of(routine(10L, null)));
        given(routineItemRepository.findAllByRoutineIdOrderBySortOrder(10L))
                .willReturn(List.of());

        TodayDirectionResult result = service.getToday(USER_ID);

        assertThat(result.emoji()).isEqualTo("🌿");
        assertThat(result.sections()).allSatisfy(section -> assertThat(section.items()).isEmpty());
    }

    @Test
    void throwsRoutineNotFoundWhenTodayRoutineDoesNotExist() {
        given(dailyRoutineRepository.findByUserIdAndRoutineDate(USER_ID, TODAY))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getToday(USER_ID))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ROUTINE_NOT_FOUND));
    }

    private DailyRoutine routine(Long id, Long weatherSnapshotId) {
        return new DailyRoutine(
                id, USER_ID, weatherSnapshotId, TODAY, RoutineStatus.READY,
                "오늘은 자외선이 강한 날씨예요!",
                "아침 보습과 자외선 차단에 신경 쓰는 것이 좋아요.",
                null, null, null, null, null, "gpt", "v1", NOW, null, NOW, NOW);
    }

    private RoutineItem item(Long id, String timeSlot, String title, int sortOrder) {
        return new RoutineItem(
                id, 10L, timeSlot, "LIFESTYLE", title, title + "를 실천해 주세요.",
                null, null, sortOrder, false, null, NOW, NOW);
    }

    private WeatherSnapshot weather(String condition, String temperature, String uvIndex) {
        return new WeatherSnapshot(
                20L, USER_ID, TODAY, "서울특별시", "중구",
                null, null, new BigDecimal(temperature), new BigDecimal(uvIndex),
                condition, "test", NOW, NOW);
    }
}
