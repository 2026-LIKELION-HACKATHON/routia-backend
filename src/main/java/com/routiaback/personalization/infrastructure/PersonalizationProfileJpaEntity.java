package com.routiaback.personalization.infrastructure;

import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.LocationSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity(name = "PersonalizationProfile")
@Table(name = "user_profiles")
class PersonalizationProfileJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal height;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", length = 20)
    private AgeGroup ageGroup;

    @Column(name = "profile_image_key", length = 500)
    private String profileImageKey;

    @Column(name = "region_sido", length = 50)
    private String regionSido;

    @Column(name = "region_sigungu", length = 50)
    private String regionSigungu;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_source", length = 20)
    private LocationSource locationSource;

    @Column(name = "location_updated_at")
    private Instant locationUpdatedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PersonalizationProfileJpaEntity() {
    }

    PersonalizationProfileJpaEntity(
            Long userId,
            BigDecimal height,
            BigDecimal weight,
            Gender gender,
            AgeGroup ageGroup,
            String profileImageKey,
            String regionSido,
            String regionSigungu,
            BigDecimal latitude,
            BigDecimal longitude,
            LocationSource locationSource,
            Instant locationUpdatedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.userId = userId;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.profileImageKey = profileImageKey;
        this.regionSido = regionSido;
        this.regionSigungu = regionSigungu;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationSource = locationSource;
        this.locationUpdatedAt = locationUpdatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    Long userId() { return userId; }
    BigDecimal height() { return height; }
    BigDecimal weight() { return weight; }
    Gender gender() { return gender; }
    AgeGroup ageGroup() { return ageGroup; }
    String profileImageKey() { return profileImageKey; }
    String regionSido() { return regionSido; }
    String regionSigungu() { return regionSigungu; }
    BigDecimal latitude() { return latitude; }
    BigDecimal longitude() { return longitude; }
    LocationSource locationSource() { return locationSource; }
    Instant locationUpdatedAt() { return locationUpdatedAt; }
    Instant createdAt() { return createdAt; }
    Instant updatedAt() { return updatedAt; }
}
