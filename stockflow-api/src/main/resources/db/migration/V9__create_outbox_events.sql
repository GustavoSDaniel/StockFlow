-- =========================================================================
-- V9__create_outbox_events.sql (CORRIGIDO)
-- Tabela de outbox para o padrão Transactional Outbox
-- =========================================================================

CREATE TABLE outbox_events (
                               id            UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                               aggregate_id  UUID          NOT NULL,
                               event_type    VARCHAR(100)  NOT NULL,
                               payload       JSONB         NOT NULL,
                               topic         VARCHAR(255)  NOT NULL,
                               partition_key VARCHAR(255)  NOT NULL,
                               created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                               created_by    UUID,                          -- ← auditoria (usuário criador)
                               processed     BOOLEAN       NOT NULL DEFAULT FALSE,
                               processed_at  TIMESTAMPTZ,
                               retry_count   INTEGER       NOT NULL DEFAULT 0,
                               last_error    TEXT
);

-- Índice parcial para polling eficiente
CREATE INDEX idx_outbox_pending
    ON outbox_events(created_at) WHERE processed = FALSE;