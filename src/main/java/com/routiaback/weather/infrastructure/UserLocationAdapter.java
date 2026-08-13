package com.routiaback.weather.infrastructure;

import com.routiaback.weather.application.port.UserLocationPort;
import com.routiaback.weather.domain.UserLocation;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class UserLocationAdapter implements UserLocationPort {

    private final UserProfileJpaRepository userProfileJpaRepository;

    UserLocationAdapter(UserProfileJpaRepository userProfileJpaRepository) {
        this.userProfileJpaRepository = userProfileJpaRepository;
    }

    @Override
    public Optional<UserLocation> findByUserId(Long userId) {
        return userProfileJpaRepository.findById(userId)
                .map(e -> new UserLocation(e.getLatitude(), e.getLongitude(), e.getRegionSido(), e.getRegionSigungu()));
    }
}