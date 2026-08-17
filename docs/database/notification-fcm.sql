CREATE TABLE push_devices (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    firebase_installation_id VARCHAR(255) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_push_devices_installation_id UNIQUE (firebase_installation_id),
    INDEX idx_push_devices_user_active (user_id, is_active),
    CONSTRAINT fk_push_devices_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_daily_routines_notification_due
    ON daily_routines (status, notification_scheduled_at);

CREATE TABLE notification_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    routine_id BIGINT UNSIGNED NOT NULL,
    push_device_id BIGINT UNSIGNED NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at DATETIME(6) NULL,
    error_code VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_notification_logs_delivery UNIQUE (routine_id, push_device_id, notification_type),
    CONSTRAINT fk_notification_logs_routine FOREIGN KEY (routine_id) REFERENCES daily_routines(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_logs_device FOREIGN KEY (push_device_id) REFERENCES push_devices(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
