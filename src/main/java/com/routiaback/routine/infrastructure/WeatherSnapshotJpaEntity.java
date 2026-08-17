package com.routiaback.routine.infrastructure;

import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*; import lombok.*;
@Entity @Table(name="weather_snapshots") @Getter @NoArgsConstructor
class WeatherSnapshotJpaEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="user_id",nullable=false) private Long userId; @Column(name="target_date",nullable=false) private LocalDate targetDate;
    @Column(name="region_sido") private String regionSido; @Column(name="region_sigungu") private String regionSigungu;
    private BigDecimal latitude; private BigDecimal longitude; @Column(name="temperature_c") private BigDecimal temperature;
    @Column(name="uv_index") private BigDecimal uvIndex; @Column(name="weather_condition") private String weatherCondition;
    private String provider; @Column(name="observed_at") private Instant observedAt; @Column(name="created_at",nullable=false) private Instant createdAt;
    WeatherSnapshotJpaEntity(Long id,Long userId,LocalDate targetDate,String sido,String sigungu,BigDecimal latitude,BigDecimal longitude,BigDecimal temperature,BigDecimal uvIndex,String condition,String provider,Instant observedAt,Instant createdAt){this.id=id;this.userId=userId;this.targetDate=targetDate;this.regionSido=sido;this.regionSigungu=sigungu;this.latitude=latitude;this.longitude=longitude;this.temperature=temperature;this.uvIndex=uvIndex;this.weatherCondition=condition;this.provider=provider;this.observedAt=observedAt;this.createdAt=createdAt;}
}
