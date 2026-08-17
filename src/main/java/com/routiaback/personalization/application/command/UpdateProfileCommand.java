package com.routiaback.personalization.application.command;

import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.LocationSource;
import java.math.BigDecimal;

public record UpdateProfileCommand(
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
}
