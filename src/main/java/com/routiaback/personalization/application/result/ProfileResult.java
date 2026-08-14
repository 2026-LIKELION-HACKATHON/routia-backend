package com.routiaback.personalization.application.result;

import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.LocationSource;
import com.routiaback.personalization.domain.UserProfile;
import java.math.BigDecimal;
import java.time.Instant;

public record ProfileResult(
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

    public static ProfileResult from(UserProfile profile) {
        return new ProfileResult(profile.height(), profile.weight(), profile.gender(), profile.ageGroup(),
                profile.profileImageKey(), profile.regionSido(), profile.regionSigungu(), profile.latitude(),
                profile.longitude(), profile.locationSource(), profile.locationUpdatedAt());
    }
}
