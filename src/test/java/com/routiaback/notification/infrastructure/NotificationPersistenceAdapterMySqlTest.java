package com.routiaback.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.routiaback.notification.application.PushDeviceService;
import com.routiaback.notification.application.port.NotificationLogRepositoryPort;
import com.routiaback.notification.domain.NotificationStatus;
import com.routiaback.notification.domain.NotificationType;
import com.routiaback.notification.domain.PushPlatform;
import java.time.Instant;
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
class NotificationPersistenceAdapterMySqlTest {
    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("routia")
            .withUsername("routia")
            .withPassword("routia")
            .withInitScript("db/auth-schema.sql");

    @Autowired PushDeviceService devices;
    @Autowired NotificationLogRepositoryPort logs;
    @Autowired JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @BeforeEach
    void createUsers() {
        jdbcTemplate.update("""
                INSERT INTO users (id, email, password_hash, name, account_status, created_at, updated_at)
                VALUES
                    (1, 'one@example.com', 'hash', 'One', 'ACTIVE', NOW(6), NOW(6)),
                    (2, 'two@example.com', 'hash', 'Two', 'ACTIVE', NOW(6), NOW(6))
                """);
    }

    @Test
    void transfersGloballyUniqueFidToLatestUser() {
        PushDeviceService.DeviceResult first = devices.register(1L, 1L, "shared-fid", PushPlatform.WEB);
        PushDeviceService.DeviceResult second = devices.register(2L, 2L, "shared-fid", PushPlatform.WEB);

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM push_devices", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT user_id FROM push_devices WHERE firebase_installation_id = 'shared-fid'", Long.class))
                .isEqualTo(2L);
    }

    @Test
    void uniqueDeliveryReservationPreventsDuplicateNotificationLog() {
        PushDeviceService.DeviceResult device = devices.register(1L, 1L, "fid-1", PushPlatform.WEB);
        jdbcTemplate.update("""
                INSERT INTO daily_routines
                    (id, user_id, routine_date, status, difficulty_snapshot,
                     notification_scheduled_at, created_at, updated_at)
                VALUES
                    (100, 1, '2026-08-17', 'READY', 'SIMPLE', NOW(6), NOW(6), NOW(6))
                """);
        Instant now = Instant.parse("2026-08-17T00:00:00Z");

        boolean first = logs.reserve(100L, device.id(), NotificationType.DAILY_ROUTINE_READY, now);
        boolean duplicate = logs.reserve(100L, device.id(), NotificationType.DAILY_ROUTINE_READY, now);

        assertThat(first).isTrue();
        assertThat(duplicate).isFalse();
        assertThat(logs.find(100L, device.id(), NotificationType.DAILY_ROUTINE_READY).orElseThrow().status())
                .isEqualTo(NotificationStatus.PENDING);
    }
}
