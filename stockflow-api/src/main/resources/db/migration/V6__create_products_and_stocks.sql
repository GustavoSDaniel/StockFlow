-- =========================================================================
-- V6__create_products_and_stocks.sql
-- Tabelas de produtos e estoques
-- Cada produto tem 1 estoque por armazém (1:N, UNIQUE em product_id + warehouse_id)
-- =========================================================================

-- ==========================================
-- PRODUTOS
-- ==========================================
CREATE TABLE products (
                          id           UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                          version      BIGINT,
                          name         VARCHAR(255)  NOT NULL,
                          description  TEXT,
                          sku          VARCHAR(100)  NOT NULL UNIQUE,
                          barcode      VARCHAR(255),
                          category_id  UUID          NOT NULL REFERENCES categories(id),
                          supplier_id  UUID          NOT NULL REFERENCES suppliers(id),
                          cost_price   NUMERIC(12,4) NOT NULL CHECK (cost_price >= 0),
                          sale_price   NUMERIC(12,4) NOT NULL CHECK (sale_price >= 0),
                          unit_measure VARCHAR(50)   NOT NULL,
                          status       VARCHAR(50)   NOT NULL DEFAULT 'ACTIVE',
                          created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                          updated_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                          created_by   UUID,
                          updated_by   UUID,
                          CONSTRAINT chk_sale_price_gte_cost CHECK (sale_price >= cost_price)
);

-- ==========================================
-- ESTOQUES
-- ==========================================
CREATE TABLE stocks (
                        id               UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                        version          BIGINT,
                        product_id       UUID         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                        current_quantity INTEGER      NOT NULL DEFAULT 0 CHECK (current_quantity >= 0),
                        minimum_quantity INTEGER      NOT NULL DEFAULT 0 CHECK (minimum_quantity >= 0),
                        maximum_quantity INTEGER      NOT NULL DEFAULT 999999999 CHECK (maximum_quantity >= 0),
                        reorder_point    INTEGER      NOT NULL DEFAULT 0 CHECK (reorder_point >= 0),
                        reorder_quantity INTEGER      NOT NULL DEFAULT 0 CHECK (reorder_quantity >= 0),
                        location         VARCHAR(255),
                        warehouse_id     VARCHAR(255) NOT NULL,
                        created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                        updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                        created_by       UUID,
                        updated_by       UUID,
                        CONSTRAINT uq_stocks_product_warehouse UNIQUE (product_id, warehouse_id),
                        CONSTRAINT chk_max_gte_min CHECK (maximum_quantity >= minimum_quantity),
                        CONSTRAINT chk_reorder_lte_min CHECK (reorder_point <= minimum_quantity)
);

-- ==========================================
-- ÍNDICES
-- ==========================================

-- products
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_supplier_id ON products(supplier_id);
CREATE INDEX idx_products_status      ON products(status);
CREATE INDEX idx_products_sku         ON products(sku);

-- stocks
CREATE INDEX idx_stocks_product_warehouse ON stocks(product_id, warehouse_id);

-- Índice para busca de estoque baixo (corrigido)
CREATE INDEX idx_stocks_low_stock
    ON stocks(current_quantity, minimum_quantity)
    WHERE current_quantity <= minimum_quantity;