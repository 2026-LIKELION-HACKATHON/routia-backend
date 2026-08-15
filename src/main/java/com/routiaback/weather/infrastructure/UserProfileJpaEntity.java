package com.routiaback.weather.infrastructure;

import jakarta.persistence.Column;
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
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "region_sido")
    private String regionSido;

    @Column(name = "region_sigungu")
    private String regionSigungu;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    public UserProfileJpaEntity(Long userId, String regionSido, String regionSigungu, BigDecimal latitude, BigDecimal longitude) {
        this.userId = userId;
        this.regionSido = regionSido;
        this.regionSigungu = regionSigungu;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
