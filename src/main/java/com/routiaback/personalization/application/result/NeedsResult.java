package com.routiaback.personalization.application.result;

import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import com.routiaback.personalization.domain.UserPreference;
import java.util.List;

public record NeedsResult(
        BodyGoal bodyGoal,
        List<BodyGoal> bodyGoals,
        List<String> bodyConcerns,
        SkinType skinType,
        List<String> skinConcerns,
        List<String> ownedTools,
        RoutineTimePreference routineTimePreference,
        RoutineDifficulty routineDifficulty
) {
    public NeedsResult(BodyGoal bodyGoal, List<String> bodyConcerns, SkinType skinType,
            List<String> skinConcerns, RoutineTimePreference routineTimePreference,
            RoutineDifficulty routineDifficulty) {
        this(bodyGoal, bodyGoal == null ? List.of() : List.of(bodyGoal), bodyConcerns, skinType,
                skinConcerns, List.of(), routineTimePreference, routineDifficulty);
    }

    public static NeedsResult from(UserPreference preference, List<BodyGoal> bodyGoals,
            List<String> bodyConcerns, List<String> skinConcerns, List<String> ownedTools) {
        List<BodyGoal> goals = bodyGoals.isEmpty() && preference.bodyGoal() != null
                ? List.of(preference.bodyGoal()) : List.copyOf(bodyGoals);
        return new NeedsResult(preference.bodyGoal(), goals, List.copyOf(bodyConcerns), preference.skinType(),
                List.copyOf(skinConcerns), List.copyOf(ownedTools), preference.routineTimePreference(),
                preference.routineDifficulty());
    }
}
