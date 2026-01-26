-- =========================
-- ROLES TABLE
-- =========================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE);

-- =========================
-- USER_ROLES (ElementCollection)
-- =========================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
);

-- =========================
-- DEFAULT ROLES
-- =========================
INSERT IGNORE INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN'),
('ROLE_SELLER');

