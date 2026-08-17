package com.routiaback.personalization.domain;

import java.math.BigDecimal;

public record UserProfilePatch(
        BigDecimal height,
        BigDecimal weight,
        Gender gender,
        AgeGroup ageGroup,
        String regionSido,
        String regionSigungu,
        BigDecimal latitude,
        BigDecimal longitude,
        LocationSource locationSource
) {

    public boolean hasLocationChange() {
        return regionSido != null || regionSigungu != null || latitude != null
                || longitude != null || locationSource != null;
    }
}
