package com.routiaback.personalization.application.command;

import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import java.util.List;

public record UpdateNeedsCommand(
        BodyGoal bodyGoal,
        List<String> bodyConcerns,
        SkinType skinType,
        List<String> skinConcerns,
        RoutineTimePreference routineTimePreference,
        RoutineDifficulty routineDifficulty,
        List<BodyGoal> bodyGoals,
        List<String> ownedTools
) {
    public UpdateNeedsCommand {
        bodyGoals = bodyGoals == null ? null : List.copyOf(bodyGoals);
        ownedTools = ownedTools == null ? null : List.copyOf(ownedTools);
    }

    public UpdateNeedsCommand(BodyGoal bodyGoal, List<String> bodyConcerns, SkinType skinType,
            List<String> skinConcerns, RoutineTimePreference routineTimePreference,
            RoutineDifficulty routineDifficulty) {
        this(bodyGoal, bodyConcerns, skinType, skinConcerns, routineTimePreference,
                routineDifficulty, null, null);
    }
}
