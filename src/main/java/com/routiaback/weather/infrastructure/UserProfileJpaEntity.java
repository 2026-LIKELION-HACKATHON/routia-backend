package com.routiaback.weather.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor
class UserProfileJpaEntity {

    @Id
    private Long userId;

    private String regionSido;
    private String regionSigungu;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public UserProfileJpaEntity(Long userId, String regionSido, String regionSigungu, BigDecimal latitude, BigDecimal longitude) {
        this.userId = userId;
        this.regionSido = regionSido;
        this.regionSigungu = regionSigungu;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}