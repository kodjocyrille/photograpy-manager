-- =============================================
-- PHOTOAGENCE — V1: Table utilisateurs internes
-- =============================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'PHOTOGRAPHER', 'ASSISTANT')),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Index pour la recherche par email (login)
CREATE INDEX idx_users_email ON users(email);

-- Insertion de l'admin par défaut (mot de passe: admin123, encodé BCrypt)
-- Note: Le DataSeeder Java gère l'insertion en dev. Cette insertion est pour prod.
INSERT INTO users (full_name, email, password_hash, role, active)
VALUES ('Admin PHOTOAGENCE', 'admin@photoagence.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'ADMIN', true);
