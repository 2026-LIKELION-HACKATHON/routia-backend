package com.routiaback.home.application;

import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.auth.domain.User;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.home.application.result.HomeResult;
import com.routiaback.home.application.result.HomeResult.TaskPreview;
import com.routiaback.routine.application.RoutineService;
import com.routiaback.routine.application.result.RoutineTodayResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final UserRepositoryPort userRepository;
    private final RoutineService routineService;

    public HomeResult getHome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        RoutineTodayResult routine;
        try {
            routine = routineService.getToday(userId);
        } catch (ApiException e) {
            routine = new RoutineTodayResult(LocalDate.now(), null, null, 0, 0, List.of());
        }

        int progressPercent = routine.totalCount() == 0
                ? 0
                : (int) Math.round(routine.completedCount() * 100.0 / routine.totalCount());

        List<TaskPreview> preview = routine.items().stream()
                .limit(3)
                .map(i -> new TaskPreview(i.itemId(), i.title(), i.completed()))
                .toList();

        return new HomeResult(
                user.name(), LocalDate.now(), routine.directionText(), routine.homeComment(),
                progressPercent, routine.completedCount(), routine.totalCount(), preview
        );
    }
}