package com.routiaback.onboarding.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.RoutineSchedule;
import com.routiaback.onboarding.application.port.OnboardingProgressRepositoryPort;
import com.routiaback.onboarding.domain.OnboardingProgress;
import java.time.Instant;
import java.time.LocalTime;
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
class OnboardingPersistenceAdapterMySqlTest {

    private static final Instant NOW = Instant.parse("2026-08-15T00:00:00Z");

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("routia")
            .withUsername("routia")
            .withPassword("routia")
            .withInitScript("db/auth-schema.sql");

    @Autowired
    private OnboardingProgressRepositoryPort progressRepository;

    @Autowired
    private RoutineScheduleRepositoryPort scheduleRepository;

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
    void persistsAndRestoresOnboardingProgress() {
        OnboardingProgress progress = OnboardingProgress.notStarted(1L, NOW)
                .completeStep1(NOW)
                .completeStep2(NOW.plusSeconds(60));

        progressRepository.save(progress);
        OnboardingProgress found = progressRepository.findByUserId(1L).orElseThrow();

        assertThat(found.lastCompletedStep()).isEqualTo(2);
        assertThat(found.step1CompletedAt()).isEqualTo(NOW);
        assertThat(found.step2CompletedAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void persistsAndUpdatesOneSchedulePerUser() {
        RoutineSchedule schedule = RoutineSchedule.create(
                1L, LocalTime.MIDNIGHT, Instant.parse("2026-08-15T15:00:00Z"), NOW);
        scheduleRepository.save(schedule);
        scheduleRepository.save(schedule.update(
                LocalTime.of(23, 59), Instant.parse("2026-08-16T14:59:00Z"), NOW.plusSeconds(60)));

        RoutineSchedule found = scheduleRepository.findByUserId(1L).orElseThrow();

        assertThat(found.notificationTime()).isEqualTo(LocalTime.of(23, 59));
        assertThat(found.nextGenerationAt()).isEqualTo(Instant.parse("2026-08-16T14:59:00Z"));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM routine_schedules WHERE user_id = 1", Integer.class)).isEqualTo(1);
    }
}
