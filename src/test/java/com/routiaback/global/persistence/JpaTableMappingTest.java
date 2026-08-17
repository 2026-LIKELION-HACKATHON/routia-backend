package com.routiaback.global.persistence;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

class JpaTableMappingTest {

    @Test
    void buildsMetadataWhenWeatherAndPersonalizationShareUserProfilesTable() throws Exception {
        Class<?> weatherProfile = Class.forName(
                "com.routiaback.weather.infrastructure.UserProfileJpaEntity");
        Class<?> personalizationProfile = Class.forName(
                "com.routiaback.personalization.infrastructure.PersonalizationProfileJpaEntity");
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false")
                .applySetting("hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .build();

        try {
            assertThatCode(() -> new MetadataSources(registry)
                    .addAnnotatedClass(weatherProfile)
                    .addAnnotatedClass(personalizationProfile)
                    .buildMetadata())
                    .doesNotThrowAnyException();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
