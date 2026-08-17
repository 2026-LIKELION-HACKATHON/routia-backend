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
    public static final int DIRECTION_TEXT_MAX_LENGTH = 1_000;
    public static final int HOME_COMMENT_MAX_LENGTH = 1_000;
    public static final int TITLE_MAX_LENGTH = 150;
    public static final int DETAIL_MAX_LENGTH = 4_000;
    public static final int EFFECT_CODE_MAX_LENGTH = 30;
    public static final int EXPECTED_EFFECT_MAX_LENGTH = 255;

    public void validate(GeneratedRoutine routine, RoutineDifficulty difficulty) {
        if (routine == null
                || invalidText(routine.directionText(), DIRECTION_TEXT_MAX_LENGTH)
                || invalidText(routine.homeComment(), HOME_COMMENT_MAX_LENGTH)
                || routine.items() == null || routine.items().isEmpty()
                || routine.items().size() > maximum(difficulty)) invalid();
        for (GeneratedRoutine.GeneratedItem item : routine.items()) {
            if (item == null
                    || invalidText(item.title(), TITLE_MAX_LENGTH)
                    || invalidText(item.detail(), DETAIL_MAX_LENGTH)
                    || invalidText(item.effectCode(), EFFECT_CODE_MAX_LENGTH)
                    || invalidText(item.expectedEffect(), EXPECTED_EFFECT_MAX_LENGTH)
                    || !names(RoutineTimeSlot.values()).contains(item.timeSlot())
                    || !names(RoutineCategory.values()).contains(item.category())) invalid();
        }
    }
    private boolean invalidText(String value, int maxLength) {
        return value == null || value.isBlank() || value.length() > maxLength;
    }
    private int maximum(RoutineDifficulty difficulty) {
        if (difficulty == null) throw new ApiException(ErrorCode.ROUTINE_GENERATION_INPUT_INVALID);
        return switch (difficulty) { case COMPLEX -> 12; case SIMPLE -> 8; case MINIMAL -> 4; };
    }
    private Set<String> names(Enum<?>[] values) { return java.util.Arrays.stream(values).map(Enum::name).collect(java.util.stream.Collectors.toSet()); }
    private void invalid() { throw new ApiException(ErrorCode.AI_RESPONSE_INVALID); }
}
