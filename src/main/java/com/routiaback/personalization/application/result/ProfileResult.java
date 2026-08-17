package com.routiaback.personalization.application.result;

import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.LocationSource;
import com.routiaback.personalization.domain.UserProfile;
import java.math.BigDecimal;
import java.time.Instant;

public record ProfileResult(
        String userName,
        BigDecimal height,
        BigDecimal weight,
        Gender gender,
        AgeGroup ageGroup,
        String profileImage,
        String regionSido,
        String regionSigungu,
        BigDecimal latitude,
        BigDecimal longitude,
        LocationSource locationSource,
        Instant locationUpdatedAt
) {
    public ProfileResult(BigDecimal height, BigDecimal weight, Gender gender, AgeGroup ageGroup,
            String profileImage, String regionSido, String regionSigungu, BigDecimal latitude,
            BigDecimal longitude, LocationSource locationSource, Instant locationUpdatedAt) {
        this(null, height, weight, gender, ageGroup, profileImage, regionSido, regionSigungu,
                latitude, longitude, locationSource, locationUpdatedAt);
    }

    public static ProfileResult from(UserProfile profile) {
        return from(profile, null);
    }

    public static ProfileResult from(UserProfile profile, String userName) {
        return new ProfileResult(userName, profile.height(), profile.weight(), profile.gender(), profile.ageGroup(),
                profile.profileImageKey(), profile.regionSido(), profile.regionSigungu(), profile.latitude(),
                profile.longitude(), profile.locationSource(), profile.locationUpdatedAt());
    }
}
