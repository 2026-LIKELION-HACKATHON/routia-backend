package com.routiaback.routine.application.port;

import com.routiaback.routine.domain.WeatherSnapshot;

public interface WeatherSnapshotRepositoryPort { WeatherSnapshot save(WeatherSnapshot snapshot); }
