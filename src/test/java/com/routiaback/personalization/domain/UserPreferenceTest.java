package com.routiaback.personalization.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserPreferenceTest {

    @Test
    void updatesOnlySpecifiedNeeds() {
        Instant now = Instant.parse("2026-08-15T00:00:00Z");
        UserPreference preference = UserPreference.empty(1L, now)
                .update(BodyGoal.MAINTAIN, SkinType.DRY, RoutineTimePreference.MORNING,
                        RoutineDifficulty.SIMPLE, now.plusSeconds(10));

        UserPreference changed = preference.update(
                BodyGoal.FAT_LOSS, null, null, RoutineDifficulty.MINIMAL, now.plusSeconds(20));

        assertThat(changed.bodyGoal()).isEqualTo(BodyGoal.FAT_LOSS);
        assertThat(changed.skinType()).isEqualTo(SkinType.DRY);
        assertThat(changed.routineTimePreference()).isEqualTo(RoutineTimePreference.MORNING);
        assertThat(changed.routineDifficulty()).isEqualTo(RoutineDifficulty.MINIMAL);
    }
}
