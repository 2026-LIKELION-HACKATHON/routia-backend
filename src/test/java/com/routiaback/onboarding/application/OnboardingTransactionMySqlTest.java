package com.routiaback.onboarding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.routiaback.onboarding.application.command.Step1Command;
import com.routiaback.onboarding.application.command.Step0Command;
import com.routiaback.onboarding.application.command.Step2Command;
import com.routiaback.onboarding.application.command.Step3Command;
import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import com.routiaback.personalization.domain.LocationSource;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "routia.jwt.secret=test-secret-that-is-at-least-32-bytes-long"
})
@Testcontainers(disabledWithoutDocker = true)
class OnboardingTransactionMySqlTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("routia")
            .withUsername("routia")
            .withPassword("routia")
            .withInitScript("db/auth-schema.sql");

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @BeforeEach
    void setUp() {
        dropFailureTriggers();
        jdbcTemplate.update("DELETE FROM routine_schedules");
        jdbcTemplate.update("DELETE FROM onboarding_progress");
        jdbcTemplate.update("DELETE FROM user_skin_concerns");
        jdbcTemplate.update("DELETE FROM user_body_concerns");
        jdbcTemplate.update("DELETE FROM user_owned_tools");
        jdbcTemplate.update("DELETE FROM user_body_goals");
        jdbcTemplate.update("DELETE FROM user_preferences");
        jdbcTemplate.update("DELETE FROM user_profiles");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("""
                INSERT INTO users
                    (id, email, password_hash, name, account_status, created_at, updated_at)
                VALUES
                    (1, 'user@example.com', 'hash', 'Soeun', 'ACTIVE', NOW(6), NOW(6))
                """);
    }

    @AfterEach
    void tearDown() {
        dropFailureTriggers();
    }

    @Test
    void rollsBackEntireStep2WhenConcernPersistenceFails() {
        completeThroughStep1();
        jdbcTemplate.execute("""
                CREATE TRIGGER fail_body_concern BEFORE INSERT ON user_body_concerns
                FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'forced body concern failure'
                """);

        assertThatThrownBy(() -> onboardingService.completeStep2(1L, step2Command()))
                .isInstanceOf(RuntimeException.class);

        assertThat(count("user_preferences")).isZero();
        assertThat(count("user_body_concerns")).isZero();
        assertThat(lastCompletedStep()).isEqualTo(1);
    }

    @Test
    void restoresPreviousSkinConcernsWhenStep2ReplacementFails() {
        completeThroughStep1();
        onboardingService.completeStep2(1L, step2Command());
        jdbcTemplate.execute("""
                CREATE TRIGGER fail_skin_concern BEFORE INSERT ON user_skin_concerns
                FOR EACH ROW BEGIN
                    IF NEW.concern_code = 'PORE' THEN
                        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'forced skin concern failure';
                    END IF;
                END
                """);

        assertThatThrownBy(() -> onboardingService.completeStep2(
                1L, new Step2Command(SkinType.OILY, List.of("PORE"))))
                .isInstanceOf(RuntimeException.class);

        assertThat(jdbcTemplate.queryForList(
                "SELECT concern_code FROM user_skin_concerns WHERE user_id = 1", String.class))
                .containsExactly("ACNE");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT skin_type FROM user_preferences WHERE user_id = 1", String.class))
                .isEqualTo("DRY");
        assertThat(lastCompletedStep()).isEqualTo(2);
    }

    @Test
    void doesNotCompleteStep3WhenPreferencePersistenceFails() {
        completeThroughStep1();
        onboardingService.completeStep2(1L, step2Command());
        jdbcTemplate.execute("""
                CREATE TRIGGER fail_preference BEFORE UPDATE ON user_preferences
                FOR EACH ROW BEGIN
                    IF NEW.routine_difficulty IS NOT NULL THEN
                        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'forced preference failure';
                    END IF;
                END
                """);

        assertThatThrownBy(() -> onboardingService.completeStep3(1L, new Step3Command(
                RoutineTimePreference.MORNING, RoutineDifficulty.SIMPLE)))
                .isInstanceOf(RuntimeException.class);

        assertThat(count("routine_schedules")).isZero();
        assertThat(lastCompletedStep()).isEqualTo(2);
    }

    private Step1Command step1Command() {
        return new Step1Command(new BigDecimal("165.5"), new BigDecimal("55.2"), Gender.FEMALE,
                AgeGroup.TWENTIES, "서울특별시", "중구", new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"), LocationSource.MANUAL);
    }

    private Step2Command step2Command() {
        return new Step2Command(SkinType.DRY, List.of("ACNE"), List.of("FACE_FASCIA_TOOL"),
                List.of("SWELLING"), List.of(BodyGoal.MAINTAIN));
    }

    private void completeThroughStep1() {
        onboardingService.completeStep0(1L, new Step0Command("Soeun", null));
        onboardingService.completeStep1(1L, step1Command());
    }

    private int count(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    private int lastCompletedStep() {
        return jdbcTemplate.queryForObject(
                "SELECT last_completed_step FROM onboarding_progress WHERE user_id = 1", Integer.class);
    }

    private void dropFailureTriggers() {
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS fail_body_concern");
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS fail_skin_concern");
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS fail_schedule");
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS fail_preference");
    }
}
