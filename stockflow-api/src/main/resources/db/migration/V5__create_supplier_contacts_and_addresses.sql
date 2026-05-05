-- =========================================================================
-- V5__create_supplier_contacts_and_addresses.sql
-- Tabelas dependentes de suppliers (1 supplier : N contacts / N addresses)
-- =========================================================================

-- ==========================================
-- CONTATOS DO FORNECEDOR
-- ==========================================
CREATE TABLE supplier_contact (
                                  id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  version      BIGINT,
    -- ON DELETE CASCADE: se o fornecedor for deletado, contatos são deletados
                                  supplier_id  UUID         NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
                                  contact_name VARCHAR(255) NOT NULL,
                                  email        VARCHAR(255),
                                  phone_number VARCHAR(50),
                                  active       BOOLEAN      NOT NULL DEFAULT TRUE,
                                  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                  updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                  created_by   UUID         REFERENCES users(id) ON DELETE SET NULL,
                                  updated_by   UUID         REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- ENDEREÇOS DO FORNECEDOR
-- ==========================================
CREATE TABLE addresses (
                           id            UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                           version       BIGINT,
    -- ON DELETE CASCADE: se o fornecedor for deletado, endereços são deletados
                           supplier_id   UUID         NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
                           label         VARCHAR(100),
                           street        VARCHAR(255) NOT NULL,
                           street_number VARCHAR(50),
                           complement    VARCHAR(255),
                           neighborhood  VARCHAR(100),
                           city          VARCHAR(100) NOT NULL,
                           zip_code      VARCHAR(20)  NOT NULL,
                           state         VARCHAR(2)   NOT NULL,
                           country       VARCHAR(100) NOT NULL DEFAULT 'Brasil',
                           is_main       BOOLEAN      NOT NULL DEFAULT FALSE,
                           active        BOOLEAN      NOT NULL DEFAULT TRUE,
                           created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                           updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                           created_by    UUID         REFERENCES users(id) ON DELETE SET NULL,
                           updated_by    UUID         REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- ÍNDICES
-- ==========================================

-- supplier_contact
CREATE INDEX idx_contacts_supplier_id ON supplier_contact(supplier_id);
CREATE INDEX idx_contacts_active      ON supplier_contact(supplier_id, active)
    WHERE active = TRUE;

-- addresses
CREATE INDEX idx_addresses_supplier_id ON addresses(supplier_id);

-- Garante apenas 1 endereço principal por fornecedor
-- Índice único parcial: só considera registros onde is_main = TRUE
CREATE UNIQUE INDEX idx_unique_main_address
    ON addresses(supplier_id) WHERE is_main = TRUE;