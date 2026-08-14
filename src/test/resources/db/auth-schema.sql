CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified_at DATETIME(6) NULL,
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE email_verifications (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    purpose VARCHAR(30) NOT NULL DEFAULT 'SIGNUP',
    code_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    verified_at DATETIME(6) NULL,
    consumed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_email_verifications_email_purpose_expires (
        email,
        purpose,
        expires_at
    )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_profiles (
    user_id BIGINT UNSIGNED NOT NULL,
    height_cm DECIMAL(5,1) NULL,
    weight_kg DECIMAL(5,1) NULL,
    gender VARCHAR(20) NULL,
    age_group VARCHAR(20) NULL,
    profile_image_key VARCHAR(500) NULL,
    region_sido VARCHAR(50) NULL,
    region_sigungu VARCHAR(50) NULL,
    latitude DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL,
    location_source VARCHAR(20) NULL,
    location_updated_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_preferences (
    user_id BIGINT UNSIGNED NOT NULL,
    body_goal VARCHAR(30) NULL,
    skin_type VARCHAR(30) NULL,
    routine_time_preference VARCHAR(30) NULL,
    routine_difficulty VARCHAR(30) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE body_concerns (
    code VARCHAR(30) NOT NULL,
    name VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE skin_concerns (
    code VARCHAR(30) NOT NULL,
    name VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_body_concerns (
    user_id BIGINT UNSIGNED NOT NULL,
    concern_code VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, concern_code),
    CONSTRAINT fk_user_body_concerns_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_body_concerns_concern FOREIGN KEY (concern_code) REFERENCES body_concerns(code) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_skin_concerns (
    user_id BIGINT UNSIGNED NOT NULL,
    concern_code VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, concern_code),
    CONSTRAINT fk_user_skin_concerns_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_skin_concerns_concern FOREIGN KEY (concern_code) REFERENCES skin_concerns(code) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE daily_routines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    routine_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    direction_text TEXT NULL,
    home_comment VARCHAR(1000) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE routine_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    routine_id BIGINT NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    detail TEXT NULL,
    sort_order INT NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO body_concerns (code, name, sort_order, active) VALUES
    ('SWELLING', '붓기', 1, TRUE),
    ('FATIGUE', '피로감', 2, TRUE),
    ('INACTIVE_BODY', '비활성', 99, FALSE);

INSERT INTO skin_concerns (code, name, sort_order, active) VALUES
    ('ACNE', '여드름', 1, TRUE),
    ('PORE', '모공', 2, TRUE),
    ('INACTIVE_SKIN', '비활성', 99, FALSE);
