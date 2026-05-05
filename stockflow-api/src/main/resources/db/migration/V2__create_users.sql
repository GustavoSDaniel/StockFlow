-- =========================================================================
-- V2__create_users.sql
-- Tabela de usuários — criada antes de todas as outras pois
-- todas as tabelas referenciam users(id) para auditoria
-- =========================================================================

CREATE TABLE users (
                       id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                       version     BIGINT,
                       keycloak_id VARCHAR(255) NOT NULL UNIQUE,   -- ID único vindo do Keycloak
                       user_name   VARCHAR(255) NOT NULL UNIQUE,   -- username único no sistema
                       created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    -- Sem FK em created_by/updated_by pois o primeiro usuário
    -- não tem criador (bootstrap do sistema via Keycloak)
                       created_by  UUID,
                       updated_by  UUID
);

-- ==========================================
-- ÍNDICES
-- ==========================================
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_user_name   ON users(user_name);
