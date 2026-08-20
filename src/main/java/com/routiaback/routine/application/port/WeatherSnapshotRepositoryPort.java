package com.routiaback.routine.application.port;

import com.routiaback.routine.domain.WeatherSnapshot;
import java.util.Optional;

public interface WeatherSnapshotRepositoryPort {
    Optional<WeatherSnapshot> findWeatherById(Long id);
    WeatherSnapshot save(WeatherSnapshot snapshot);
}
