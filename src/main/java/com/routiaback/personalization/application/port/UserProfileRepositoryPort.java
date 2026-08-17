package com.routiaback.personalization.application.port;

import com.routiaback.personalization.domain.UserProfile;
import java.util.Optional;

public interface UserProfileRepositoryPort {

    Optional<UserProfile> findByUserId(Long userId);

    UserProfile save(UserProfile profile);
}
