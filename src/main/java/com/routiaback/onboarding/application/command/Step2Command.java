package com.routiaback.onboarding.application.command;

import com.routiaback.personalization.domain.SkinType;
import com.routiaback.personalization.domain.BodyGoal;
import java.util.List;

public record Step2Command(SkinType skinType, List<String> skinConcerns,
        List<String> ownedTools, List<String> bodyConcerns, List<BodyGoal> bodyGoals) {

    public Step2Command {
        skinConcerns = skinConcerns == null ? null : List.copyOf(skinConcerns);
        ownedTools = ownedTools == null ? null : List.copyOf(ownedTools);
        bodyConcerns = bodyConcerns == null ? null : List.copyOf(bodyConcerns);
        bodyGoals = bodyGoals == null ? null : List.copyOf(bodyGoals);
    }

    public Step2Command(SkinType skinType, List<String> skinConcerns) {
        this(skinType, skinConcerns, List.of(), List.of(), null);
    }
}
