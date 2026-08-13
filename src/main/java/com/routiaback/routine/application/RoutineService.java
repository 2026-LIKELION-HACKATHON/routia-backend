package com.routiaback.routine.application;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.application.port.RoutineItemRepositoryPort;
import com.routiaback.routine.application.result.RoutineTodayResult;
import com.routiaback.routine.application.result.RoutineTodayResult.Item;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final DailyRoutineRepositoryPort dailyRoutineRepository;
    private final RoutineItemRepositoryPort routineItemRepository;

    public RoutineTodayResult getToday(Long userId) {
        LocalDate today = LocalDate.now();
        DailyRoutine dailyRoutine = dailyRoutineRepository.findByUserIdAndRoutineDate(userId, today)
                .orElseThrow(() -> new ApiException(ErrorCode.ROUTINE_NOT_FOUND));

        List<RoutineItem> routineItems = routineItemRepository.findAllByRoutineIdOrderBySortOrder(dailyRoutine.id());

        List<Item> items = routineItems.stream()
                .map(i -> new Item(i.id(), i.title(), i.completed()))
                .toList();

        int completedCount = (int) routineItems.stream().filter(RoutineItem::completed).count();

        return new RoutineTodayResult(today, dailyRoutine.directionText(), dailyRoutine.homeComment(),
                completedCount, routineItems.size(), items);
    }
}