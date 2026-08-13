package com.routiaback.routine.application.port;

import com.routiaback.routine.domain.RoutineItem;
import java.util.List;

public interface RoutineItemRepositoryPort {
    List<RoutineItem> findAllByRoutineIdOrderBySortOrder(Long routineId);
}