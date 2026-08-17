-- 기존 RDS에 notification 초기 스키마가 일부 반영된 환경용 migration.
-- 적용 전 push_devices.fcm_token에 저장된 값이 Firebase installation ID인지 확인한다.
-- 현재 운영 RDS는 push_devices가 비어 있으므로 컬럼 이름과 길이 변경에 데이터 손실이 없다.

ALTER TABLE push_devices
    DROP INDEX uk_push_devices_fcm_token,
    CHANGE COLUMN fcm_token firebase_installation_id VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uk_push_devices_installation_id UNIQUE (firebase_installation_id);

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

-- daily_routines(status, notification_scheduled_at) 인덱스는 기존 RDS의
-- idx_daily_routines_status_notification을 그대로 사용한다.
