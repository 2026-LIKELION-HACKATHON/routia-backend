package com.routiaback.onboarding.application.command;

import com.routiaback.personalization.domain.SkinType;
import java.util.List;

public record Step2Command(SkinType skinType, List<String> skinConcerns) {

    public Step2Command {
        skinConcerns = skinConcerns == null ? null : List.copyOf(skinConcerns);
    }
}
