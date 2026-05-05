-- =========================================================================
-- V4__create_suppliers.sql
-- Tabela de fornecedores
-- =========================================================================

CREATE TABLE suppliers (
                           id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                           version         BIGINT,
                           name            VARCHAR(255)  NOT NULL,
                           cnpj            VARCHAR(14)   NOT NULL UNIQUE,
                           trade_name      VARCHAR(255),
                           website         VARCHAR(255),
                           min_order_value NUMERIC(12,2) DEFAULT 0,
                           notes           TEXT,
                           created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                           updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                           created_by      UUID          REFERENCES users(id) ON DELETE SET NULL,
                           updated_by      UUID          REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- ÍNDICES
-- ==========================================
CREATE INDEX idx_suppliers_cnpj ON suppliers(cnpj);
CREATE INDEX idx_suppliers_name ON suppliers(name);