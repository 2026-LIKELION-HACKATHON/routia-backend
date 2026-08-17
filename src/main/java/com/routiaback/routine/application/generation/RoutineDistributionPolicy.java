package com.routiaback.routine.application.generation;

import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RoutineDistributionPolicy {
    private RoutineDistributionPolicy() { }

    public static RoutineGenerationRequest.DistributionInput calculate(
            RoutineDifficulty difficulty, RoutineTimePreference preference) {
        int total = switch (difficulty) { case MINIMAL -> 4; case SIMPLE -> 8; case COMPLEX -> 12; };
        int preferred = Math.toIntExact(Math.round(total * 2.0 / 3.0));
        Map<String, Integer> targets = new LinkedHashMap<>();
        if (preference == RoutineTimePreference.MORNING) {
            targets.put("MORNING", preferred);
            targets.put("OTHER", total - preferred);
        } else if (preference == RoutineTimePreference.EVENING) {
            targets.put("EVENING_OR_BEDTIME", preferred);
            targets.put("OTHER", total - preferred);
        } else {
            int afternoon = total == 4 ? 1 : total == 8 ? 2 : 4;
            int morning = total == 4 ? 2 : (total - afternoon) / 2;
            targets.put("MORNING", morning);
            targets.put("AFTERNOON", afternoon);
            targets.put("EVENING_OR_BEDTIME", total - morning - afternoon);
        }
        return new RoutineGenerationRequest.DistributionInput(total, targets);
    }
}
