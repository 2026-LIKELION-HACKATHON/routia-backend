package com.routiaback.onboarding.application.command;

import com.routiaback.personalization.application.command.ProfileImageUpload;

public record Step0Command(String userName, ProfileImageUpload profileImage) { }
