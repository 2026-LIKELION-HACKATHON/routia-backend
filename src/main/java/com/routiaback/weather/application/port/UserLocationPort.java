package com.routiaback.weather.application.port;

import com.routiaback.weather.domain.UserLocation;
import java.util.Optional;

public interface UserLocationPort {
    Optional<UserLocation> findByUserId(Long userId);
}