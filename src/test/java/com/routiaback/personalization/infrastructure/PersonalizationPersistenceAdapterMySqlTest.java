package com.routiaback.personalization.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.LocationSource;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import com.routiaback.personalization.domain.UserPreference;
import com.routiaback.personalization.domain.UserProfile;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "routia.jwt.secret=test-secret-that-is-at-least-32-bytes-long"
})
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class PersonalizationPersistenceAdapterMySqlTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("routia")
            .withUsername("routia")
            .withPassword("routia")
            .withInitScript("db/auth-schema.sql");

    @Autowired
    private PersonalizationPersistenceAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @BeforeEach
    void createUser() {
        jdbcTemplate.update("""
                INSERT INTO users
                    (id, email, password_hash, name, account_status, created_at, updated_at)
                VALUES
                    (1, 'user@example.com', 'hash', 'Soeun', 'ACTIVE', NOW(6), NOW(6))
                """);
    }

    @Test
    void persistsAndReadsProfileUsingConfirmedSchema() {
        Instant now = Instant.parse("2026-08-15T00:00:00.123456Z");
        UserProfile profile = new UserProfile(
                1L, new BigDecimal("165.5"), new BigDecimal("55.2"), Gender.FEMALE, null,
                "1/profile.jpg", "서울특별시", "중구", new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"), LocationSource.GPS, now, now, now);

        adapter.save(profile);
        UserProfile found = adapter.findByUserId(1L).orElseThrow();

        assertThat(found.height()).isEqualByComparingTo("165.5");
        assertThat(found.latitude()).isEqualByComparingTo("37.5665000");
        assertThat(found.profileImageKey()).isEqualTo("1/profile.jpg");
    }

    @Test
    void persistsPreferenceAndReplacesConcernRelations() {
        Instant now = Instant.parse("2026-08-15T00:00:00Z");
        adapter.savePreference(new UserPreference(
                1L, BodyGoal.MAINTAIN, SkinType.DRY, RoutineTimePreference.MORNING,
                RoutineDifficulty.SIMPLE, now, now));

        adapter.replaceBodyConcerns(1L, List.of("FATIGUE", "SWELLING"));
        adapter.replaceSkinConcerns(1L, List.of("PORE", "ACNE"));

        assertThat(adapter.findPreferenceByUserId(1L).orElseThrow().bodyGoal())
                .isEqualTo(BodyGoal.MAINTAIN);
        assertThat(adapter.findBodyConcernCodes(1L)).containsExactly("SWELLING", "FATIGUE");
        assertThat(adapter.findSkinConcernCodes(1L)).containsExactly("ACNE", "PORE");

        adapter.replaceBodyConcerns(1L, List.of());
        assertThat(adapter.findBodyConcernCodes(1L)).isEmpty();
    }

    @Test
    void returnsOnlyActiveConcernCodesForValidation() {
        assertThat(adapter.findActiveBodyConcernCodes(Set.of("SWELLING", "INACTIVE_BODY", "UNKNOWN")))
                .containsExactly("SWELLING");
        assertThat(adapter.findActiveSkinConcernCodes(Set.of("ACNE", "INACTIVE_SKIN", "UNKNOWN")))
                .containsExactly("ACNE");
    }
}
