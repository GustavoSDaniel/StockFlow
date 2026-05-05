
-- =========================================================================
-- V3__create_categories.sql
-- Tabela de categorias com suporte a hierarquia (auto-relacionamento)
-- =========================================================================

CREATE TABLE categories (
                            id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                            version     BIGINT,
                            name        VARCHAR(255) NOT NULL UNIQUE,
                            description TEXT,
    -- Auto-relacionamento: categoria pode ter uma categoria pai
    -- ON DELETE SET NULL: se a pai for deletada, filhas viram raiz
                            parent_id   UUID         REFERENCES categories(id) ON DELETE SET NULL,
                            active      BOOLEAN      NOT NULL DEFAULT TRUE,
                            created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                            updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                            created_by  UUID         REFERENCES users(id) ON DELETE SET NULL,
                            updated_by  UUID         REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- ÍNDICES
-- ==========================================
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_active    ON categories(active) WHERE active = TRUE;
