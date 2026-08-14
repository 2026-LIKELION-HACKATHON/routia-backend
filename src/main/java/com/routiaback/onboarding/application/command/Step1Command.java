package com.routiaback.onboarding.application.command;

import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.Gender;
import java.math.BigDecimal;
import java.util.List;

public record Step1Command(
        BigDecimal height,
        BigDecimal weight,
        Gender gender,
        AgeGroup ageGroup,
        List<String> bodyConcerns,
        BodyGoal bodyGoal
) {

    public Step1Command {
        bodyConcerns = bodyConcerns == null ? null : List.copyOf(bodyConcerns);
    }
}
