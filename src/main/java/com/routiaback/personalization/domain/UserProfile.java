package com.routiaback.personalization.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record UserProfile(
        Long userId,
        BigDecimal height,
        BigDecimal weight,
        Gender gender,
        AgeGroup ageGroup,
        String profileImageKey,
        String regionSido,
        String regionSigungu,
        BigDecimal latitude,
        BigDecimal longitude,
        LocationSource locationSource,
        Instant locationUpdatedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserProfile empty(Long userId, Instant now) {
        return new UserProfile(userId, null, null, null, null, null, null, null,
                null, null, null, null, now, now);
    }

    public UserProfile update(UserProfilePatch patch, Instant now) {
        boolean locationChanged = patch.hasLocationChange();
        return new UserProfile(
                userId,
                valueOrCurrent(patch.height(), height),
                valueOrCurrent(patch.weight(), weight),
                valueOrCurrent(patch.gender(), gender),
                valueOrCurrent(patch.ageGroup(), ageGroup),
                profileImageKey,
                valueOrCurrent(patch.regionSido(), regionSido),
                valueOrCurrent(patch.regionSigungu(), regionSigungu),
                valueOrCurrent(patch.latitude(), latitude),
                valueOrCurrent(patch.longitude(), longitude),
                valueOrCurrent(patch.locationSource(), locationSource),
                locationChanged ? now : locationUpdatedAt,
                createdAt,
                now
        );
    }

    public UserProfile updateProfileImage(String imageKey, Instant now) {
        return new UserProfile(userId, height, weight, gender, ageGroup, imageKey,
                regionSido, regionSigungu, latitude, longitude, locationSource,
                locationUpdatedAt, createdAt, now);
    }

    private static <T> T valueOrCurrent(T value, T current) {
        return value == null ? current : value;
    }
}
