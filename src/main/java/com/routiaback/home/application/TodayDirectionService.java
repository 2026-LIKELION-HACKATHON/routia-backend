package com.routiaback.home.application;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.home.application.result.TodayDirectionResult;
import com.routiaback.home.application.result.TodayDirectionResult.Item;
import com.routiaback.home.application.result.TodayDirectionResult.Section;
import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.application.port.RoutineItemRepositoryPort;
import com.routiaback.routine.application.port.WeatherSnapshotRepositoryPort;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineItem;
import com.routiaback.routine.domain.WeatherSnapshot;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodayDirectionService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final Clock clock;
    private final DailyRoutineRepositoryPort dailyRoutineRepository;
    private final RoutineItemRepositoryPort routineItemRepository;
    private final WeatherSnapshotRepositoryPort weatherSnapshotRepository;

    public TodayDirectionResult getToday(Long userId) {
        LocalDate today = LocalDate.now(clock.withZone(SERVICE_ZONE));
        DailyRoutine routine = dailyRoutineRepository.findByUserIdAndRoutineDate(userId, today)
                .orElseThrow(() -> new ApiException(ErrorCode.ROUTINE_NOT_FOUND));

        List<RoutineItem> items = routineItemRepository.findAllByRoutineIdOrderBySortOrder(routine.id());
        WeatherSnapshot weather = routine.weatherSnapshotId() == null
                ? null
                : weatherSnapshotRepository.findWeatherById(routine.weatherSnapshotId()).orElse(null);

        return new TodayDirectionResult(
                routine.id(),
                routine.routineDate(),
                resolveEmoji(weather),
                routine.directionText(),
                routine.homeComment(),
                List.of(
                        section("MORNING", "오전", "☀️", items, List.of("MORNING")),
                        section("AFTERNOON", "오후", "🌇", items, List.of("AFTERNOON")),
                        section("NIGHT", "밤", "🌙", items, List.of("EVENING", "BEDTIME"))
                ));
    }

    private Section section(String period, String label, String icon,
                            List<RoutineItem> items, List<String> timeSlots) {
        List<Item> sectionItems = items.stream()
                .filter(item -> timeSlots.contains(item.timeSlot()))
                .map(item -> new Item(item.id(), item.timeSlot(), item.title(), item.detail()))
                .toList();
        return new Section(period, label, icon, sectionItems);
    }

    private String resolveEmoji(WeatherSnapshot weather) {
        if (weather == null) {
            return "🌿";
        }

        String condition = weather.weatherCondition() == null
                ? ""
                : weather.weatherCondition().toLowerCase(Locale.ROOT);

        if (condition.contains("뇌우") || condition.contains("thunder")) return "⛈️";
        if (condition.contains("눈") || condition.contains("snow")) return "❄️";
        if (condition.contains("비") || condition.contains("소나기") || condition.contains("rain")) return "🌧️";
        if (greaterThanOrEqual(weather.temperature(), "30")) return "🥵";
        if (lessThanOrEqual(weather.temperature(), "5")) return "🥶";
        if (greaterThanOrEqual(weather.uvIndex(), "6")) return "😎";
        if (condition.contains("맑") || condition.contains("clear") || condition.contains("sunny")) return "☀️";
        if (condition.contains("구름") || condition.contains("cloud")) return "☁️";
        return "🌿";
    }

    private boolean greaterThanOrEqual(BigDecimal value, String threshold) {
        return value != null && value.compareTo(new BigDecimal(threshold)) >= 0;
    }

    private boolean lessThanOrEqual(BigDecimal value, String threshold) {
        return value != null && value.compareTo(new BigDecimal(threshold)) <= 0;
    }
}
