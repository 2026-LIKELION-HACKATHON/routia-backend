-- Onboarding V2 / Notification Settings DB migration (MySQL 8)
-- 기존 user_preferences.body_goal은 하위 호환을 위해 유지하며 첫 번째 bodyGoals 값을 미러링한다.

CREATE TABLE IF NOT EXISTS body_goals (
    code VARCHAR(30) NOT NULL,
    name VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS owned_tools (
    code VARCHAR(40) NOT NULL,
    name VARCHAR(80) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_body_goals (
    user_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, code),
    CONSTRAINT fk_user_body_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_body_goals_goal FOREIGN KEY (code) REFERENCES body_goals(code) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_owned_tools (
    user_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(40) NOT NULL,
    PRIMARY KEY (user_id, code),
    CONSTRAINT fk_user_owned_tools_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_owned_tools_tool FOREIGN KEY (code) REFERENCES owned_tools(code) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

ALTER TABLE routine_schedules MODIFY notification_time TIME NULL;

INSERT INTO body_goals (code, name, sort_order, active) VALUES
    ('MUSCLE_GAIN', '근육 증가', 1, TRUE),
    ('MAINTAIN', '현재 상태 유지', 2, TRUE),
    ('FAT_LOSS', '체지방 감소', 3, TRUE),
    ('BUILD_HABIT', '생활 습관 형성', 4, TRUE),
    ('REGULAR_LIFE', '규칙적인 생활', 5, TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order), active = VALUES(active);

INSERT INTO owned_tools (code, name, sort_order, active) VALUES
    ('SKINCARE_ABSORPTION_DEVICE', '스킨케어 흡수 기기', 1, TRUE),
    ('BODY_FASCIA_TOOL', '바디 괄사', 2, TRUE),
    ('FACE_FASCIA_TOOL', '페이스 괄사', 3, TRUE),
    ('EXFOLIATING_PRODUCT', '각질 제거 제품', 4, TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order), active = VALUES(active);

INSERT INTO body_concerns (code, name, sort_order, active) VALUES
    ('BODY_SHAPE_CHANGE', '체형 변화', 3, TRUE),
    ('CIRCULATION', '혈액순환', 4, TRUE),
    ('WEIGHT_LOSS', '체중 감소', 5, TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order), active = VALUES(active);

INSERT INTO skin_concerns (code, name, sort_order, active) VALUES
    ('ELASTICITY', '탄력', 3, TRUE),
    ('WRINKLE', '주름', 4, TRUE),
    ('PIGMENTATION', '색소', 5, TRUE),
    ('SEBUM', '피지', 6, TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order), active = VALUES(active);

INSERT IGNORE INTO user_body_goals (user_id, code)
SELECT user_id, body_goal FROM user_preferences WHERE body_goal IS NOT NULL;
