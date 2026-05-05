-- =========================================================================
-- V6__create_products_and_stocks.sql
-- Tabelas de produtos e estoques
-- Cada produto tem exatamente 1 estoque (1:1)
-- =========================================================================

-- ==========================================
-- PRODUTOS
-- ==========================================
CREATE TABLE products (
                          id           UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
                          version      BIGINT,
                          name         VARCHAR(255)   NOT NULL,
                          description  TEXT,
                          sku          VARCHAR(100)   NOT NULL UNIQUE,   -- identificador único do produto
                          barcode      VARCHAR(255),
                          category_id  UUID           NOT NULL REFERENCES categories(id),
                          supplier_id  UUID           NOT NULL REFERENCES suppliers(id),
                          cost_price   NUMERIC(12,4)  NOT NULL CHECK (cost_price >= 0),
                          sale_price   NUMERIC(12,4)  NOT NULL CHECK (sale_price >= 0),
                          unit_measure unit_measure   NOT NULL,
                          status       product_status NOT NULL DEFAULT 'ACTIVE',
                          created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                          updated_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                          created_by   UUID           REFERENCES users(id) ON DELETE SET NULL,
                          updated_by   UUID           REFERENCES users(id) ON DELETE SET NULL,

    -- Preço de venda deve ser >= preço de custo
                          CONSTRAINT chk_sale_price_gte_cost CHECK (sale_price >= cost_price)
);

-- ==========================================
-- ESTOQUES
-- ==========================================
CREATE TABLE stocks (
                        id               UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
                        version          BIGINT,
    -- UNIQUE: 1 produto tem exatamente 1 estoque
    -- ON DELETE CASCADE: se o produto for deletado, estoque é deletado
                        product_id       UUID    NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
                        current_quantity INTEGER NOT NULL DEFAULT 0 CHECK (current_quantity >= 0),
                        minimum_quantity INTEGER NOT NULL DEFAULT 0 CHECK (minimum_quantity >= 0),
                        maximum_quantity INTEGER NOT NULL DEFAULT 999999999,
                        reorder_point    INTEGER NOT NULL DEFAULT 0 CHECK (reorder_point >= 0),
                        reorder_quantity INTEGER NOT NULL DEFAULT 0 CHECK (reorder_quantity >= 0),
                        location         VARCHAR(255),
                        warehouse_id     VARCHAR(255),
                        created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        created_by       UUID    REFERENCES users(id) ON DELETE SET NULL,
                        updated_by       UUID    REFERENCES users(id) ON DELETE SET NULL,

    -- Estoque máximo deve ser maior que o mínimo
                        CONSTRAINT chk_max_gte_min CHECK (maximum_quantity >= minimum_quantity)
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
CREATE INDEX idx_stocks_product_id ON stocks(product_id);

-- Índice para busca de estoque baixo (query mais comum do sistema)
CREATE INDEX idx_stocks_low_stock
    ON stocks(current_quantity, minimum_quantity)
    WHERE current_quantity <= minimum_quantity;