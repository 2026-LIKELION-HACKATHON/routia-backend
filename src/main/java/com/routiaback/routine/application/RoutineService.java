package com.routiaback.routine.application;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.application.port.RoutineItemRepositoryPort;
import com.routiaback.routine.application.result.DailyStat;
import com.routiaback.routine.application.result.RoutineItemToggleResult;
import com.routiaback.routine.application.result.RoutineTodayResult;
import com.routiaback.routine.application.result.RoutineTodayResult.Item;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final Clock clock;

    private final DailyRoutineRepositoryPort dailyRoutineRepository;
    private final RoutineItemRepositoryPort routineItemRepository;

    public RoutineTodayResult getToday(Long userId) {
        LocalDate today = LocalDate.now();
        DailyRoutine dailyRoutine = dailyRoutineRepository.findByUserIdAndRoutineDate(userId, today)
                .orElseThrow(() -> new ApiException(ErrorCode.ROUTINE_NOT_FOUND));

        List<RoutineItem> routineItems = routineItemRepository.findAllByRoutineIdOrderBySortOrder(dailyRoutine.id());

        List<Item> items = routineItems.stream()
                .map(i -> new Item(
                        i.id(), i.timeSlot(), i.category(), i.title(), i.detail(),
                        i.effectCode(), i.expectedEffect(), i.sortOrder(), i.completed(), i.completedAt()))
                .toList();

        int completedCount = (int) routineItems.stream().filter(RoutineItem::completed).count();

        return new RoutineTodayResult(dailyRoutine.id(), today,
                dailyRoutine.directionText(), dailyRoutine.homeComment(),
                completedCount, routineItems.size(), items);
    }

    public RoutineItemToggleResult toggleItem(Long userId, Long itemId) {
        RoutineItem item = routineItemRepository.findItemById(itemId)
                .orElseThrow(() -> new ApiException(ErrorCode.ROUTINE_ITEM_NOT_FOUND));

        DailyRoutine dailyRoutine = dailyRoutineRepository.findById(item.routineId())
                .orElseThrow(() -> new ApiException(ErrorCode.ROUTINE_NOT_FOUND));

        if (!dailyRoutine.userId().equals(userId)) {
            throw new ApiException(ErrorCode.ROUTINE_ITEM_ACCESS_DENIED);
        }

        RoutineItem toggled = item.toggleCompleted(clock.instant());
        RoutineItem saved = routineItemRepository.save(toggled);

        return new RoutineItemToggleResult(saved.id(), saved.completed());
    }

    public List<DailyStat> getStats(Long userId, LocalDate start, LocalDate end) {
        List<DailyRoutine> routines = dailyRoutineRepository.findAllByUserIdAndRoutineDateBetween(userId, start, end);
        List<Long> routineIds = routines.stream().map(DailyRoutine::id).toList();
        List<RoutineItem> allItems = routineItemRepository.findAllByRoutineIds(routineIds);

        Map<Long, List<RoutineItem>> itemsByRoutineId = allItems.stream()
                .collect(Collectors.groupingBy(RoutineItem::routineId));

        return routines.stream()
                .map(r -> {
                    List<RoutineItem> items = itemsByRoutineId.getOrDefault(r.id(), List.of());
                    int completed = (int) items.stream().filter(RoutineItem::completed).count();
                    return new DailyStat(r.routineDate(), completed, items.size());
                })
                .sorted(Comparator.comparing(DailyStat::date))
                .toList();
    }
}
