package com.routiaback.personalization.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserProfileTest {

    @Test
    void updatesPhysicalProfileWithoutChangingUnspecifiedValues() {
        Instant createdAt = Instant.parse("2026-08-15T00:00:00Z");
        Instant updatedAt = createdAt.plusSeconds(60);
        UserProfile profile = UserProfile.empty(1L, createdAt)
                .update(new UserProfilePatch(
                        new BigDecimal("165.5"),
                        new BigDecimal("55.2"),
                        Gender.FEMALE,
                        AgeGroup.TWENTIES,
                        null,
                        null,
                        null,
                        null,
                        null
                ), updatedAt);

        UserProfile changed = profile.update(new UserProfilePatch(
                null,
                new BigDecimal("54.8"),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ), updatedAt.plusSeconds(60));

        assertThat(changed.height()).isEqualByComparingTo("165.5");
        assertThat(changed.weight()).isEqualByComparingTo("54.8");
        assertThat(changed.gender()).isEqualTo(Gender.FEMALE);
        assertThat(changed.ageGroup()).isEqualTo(AgeGroup.TWENTIES);
    }

    @Test
    void recordsGpsLocationSourceAndUpdateTime() {
        Instant now = Instant.parse("2026-08-15T01:00:00Z");

        UserProfile profile = UserProfile.empty(1L, now).update(new UserProfilePatch(
                null,
                null,
                null,
                null,
                "서울특별시",
                "중구",
                new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"),
                LocationSource.GPS
        ), now.plusSeconds(10));

        assertThat(profile.locationSource()).isEqualTo(LocationSource.GPS);
        assertThat(profile.locationUpdatedAt()).isEqualTo(now.plusSeconds(10));
    }
}
