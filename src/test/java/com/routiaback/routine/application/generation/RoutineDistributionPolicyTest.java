package com.routiaback.routine.application.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import org.junit.jupiter.api.Test;

class RoutineDistributionPolicyTest {
    @Test
    void calculatesTwoThirdsForPreferredTime() {
        assertThat(RoutineDistributionPolicy.calculate(RoutineDifficulty.MINIMAL,
                RoutineTimePreference.MORNING).timeSlotTargets().get("MORNING")).isEqualTo(3);
        assertThat(RoutineDistributionPolicy.calculate(RoutineDifficulty.SIMPLE,
                RoutineTimePreference.EVENING).timeSlotTargets().get("EVENING_OR_BEDTIME")).isEqualTo(5);
        assertThat(RoutineDistributionPolicy.calculate(RoutineDifficulty.COMPLEX,
                RoutineTimePreference.MORNING).timeSlotTargets().get("MORNING")).isEqualTo(8);
    }

    @Test
    void balancesAnyPreferenceByDifficulty() {
        assertThat(RoutineDistributionPolicy.calculate(RoutineDifficulty.MINIMAL,
                RoutineTimePreference.ANY).timeSlotTargets())
                .containsEntry("MORNING", 2).containsEntry("AFTERNOON", 1)
                .containsEntry("EVENING_OR_BEDTIME", 1);
        assertThat(RoutineDistributionPolicy.calculate(RoutineDifficulty.SIMPLE,
                RoutineTimePreference.ANY).timeSlotTargets())
                .containsEntry("MORNING", 3).containsEntry("AFTERNOON", 2)
                .containsEntry("EVENING_OR_BEDTIME", 3);
        assertThat(RoutineDistributionPolicy.calculate(RoutineDifficulty.COMPLEX,
                RoutineTimePreference.ANY).timeSlotTargets())
                .containsEntry("MORNING", 4).containsEntry("AFTERNOON", 4)
                .containsEntry("EVENING_OR_BEDTIME", 4);
    }
}
