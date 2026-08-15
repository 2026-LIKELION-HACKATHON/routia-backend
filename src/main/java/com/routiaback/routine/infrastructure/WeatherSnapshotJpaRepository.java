package com.routiaback.routine.infrastructure;
import org.springframework.data.jpa.repository.JpaRepository;
interface WeatherSnapshotJpaRepository extends JpaRepository<WeatherSnapshotJpaEntity,Long>{}
