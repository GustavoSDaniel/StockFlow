-- =========================================================================
-- V7__create_inventory_movements.sql
-- Tabela de movimentações de estoque
-- IMUTÁVEL: sem version, updated_at e updated_by
-- Nenhuma movimentação pode ser alterada após criação — apenas inserção
-- =========================================================================

CREATE TABLE inventory_movement (
                                    id               UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    product_id       UUID            NOT NULL REFERENCES products(id),
                                    stock_id         UUID            NOT NULL REFERENCES stocks(id),
                                    movement_type    movement_type   NOT NULL,
                                    quantity         INTEGER         NOT NULL CHECK (quantity > 0),
                                    quantity_before  INTEGER         NOT NULL CHECK (quantity_before >= 0),
                                    quantity_after   INTEGER         NOT NULL CHECK (quantity_after >= 0),
                                    reason           movement_reason NOT NULL,
                                    reference_number VARCHAR(255),                           -- NF, pedido, etc.
                                    supplier_id      UUID            REFERENCES suppliers(id),
                                    customer_id      UUID,                                   -- ID externo do cliente
                                    note             TEXT,
                                    unit_cost        NUMERIC(12,4)   CHECK (unit_cost >= 0),
    -- Sem version  → registro imutável
    -- Sem updated_at → nunca é atualizado
    -- Sem updated_by → nunca é atualizado
                                    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
                                    created_by       UUID            REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- ÍNDICES
-- ==========================================
CREATE INDEX idx_movements_product_id    ON inventory_movement(product_id);
CREATE INDEX idx_movements_stock_id      ON inventory_movement(stock_id);
CREATE INDEX idx_movements_type          ON inventory_movement(movement_type);
CREATE INDEX idx_movements_reason        ON inventory_movement(reason);
CREATE INDEX idx_movements_created_at    ON inventory_movement(created_at DESC);
CREATE INDEX idx_movements_reference     ON inventory_movement(reference_number)
    WHERE reference_number IS NOT NULL;

-- Índice composto para histórico por produto ordenado por data
CREATE INDEX idx_movements_product_date
    ON inventory_movement(product_id, created_at DESC);