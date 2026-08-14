package com.routiaback.personalization.application.result;

import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import com.routiaback.personalization.domain.UserPreference;
import java.util.List;

public record NeedsResult(
        BodyGoal bodyGoal,
        List<String> bodyConcerns,
        SkinType skinType,
        List<String> skinConcerns,
        RoutineTimePreference routineTimePreference,
        RoutineDifficulty routineDifficulty
) {

    public static NeedsResult from(UserPreference preference, List<String> bodyConcerns, List<String> skinConcerns) {
        return new NeedsResult(preference.bodyGoal(), List.copyOf(bodyConcerns), preference.skinType(),
                List.copyOf(skinConcerns), preference.routineTimePreference(), preference.routineDifficulty());
    }
}
