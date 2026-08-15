package com.routiaback.routine.application.generation;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.routine.domain.RoutineCategory;
import com.routiaback.routine.domain.RoutineTimeSlot;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RoutineGenerationValidator {
    public void validate(GeneratedRoutine routine, RoutineDifficulty difficulty) {
        if (routine == null || routine.items() == null || routine.items().isEmpty()
                || routine.items().size() > maximum(difficulty)) invalid();
        for (GeneratedRoutine.GeneratedItem item : routine.items()) {
            if (item == null || item.title() == null || item.title().isBlank()
                    || !names(RoutineTimeSlot.values()).contains(item.timeSlot())
                    || !names(RoutineCategory.values()).contains(item.category())) invalid();
        }
    }
    private int maximum(RoutineDifficulty difficulty) {
        if (difficulty == null) throw new ApiException(ErrorCode.ROUTINE_GENERATION_INPUT_INVALID);
        return switch (difficulty) { case COMPLEX -> 12; case SIMPLE -> 8; case MINIMAL -> 4; };
    }
    private Set<String> names(Enum<?>[] values) { return java.util.Arrays.stream(values).map(Enum::name).collect(java.util.stream.Collectors.toSet()); }
    private void invalid() { throw new ApiException(ErrorCode.AI_RESPONSE_INVALID); }
}
