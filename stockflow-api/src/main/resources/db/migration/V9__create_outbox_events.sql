-- =========================================================================
-- V9__create_outbox_events.sql
-- Tabela de outbox para o padrão Transactional Outbox
-- Garante que eventos de domínio sejam publicados no Kafka de forma
-- confiável, mesmo se o broker estiver indisponível no momento da transação.
-- =========================================================================

CREATE TABLE outbox_events (
                               id            UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                               aggregate_id  UUID          NOT NULL,
                               event_type    VARCHAR(100)  NOT NULL,
                               payload       JSONB         NOT NULL,
                               topic         VARCHAR(255)  NOT NULL,
                               partition_key VARCHAR(255)  NOT NULL,
                               created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                               processed     BOOLEAN       NOT NULL DEFAULT FALSE,
                               processed_at  TIMESTAMPTZ,
                               retry_count   INTEGER       NOT NULL DEFAULT 0,
                               last_error    TEXT
);

-- Índice parcial para polling eficiente: busca apenas eventos pendentes,
-- ordenados por data de criação (FIFO)
CREATE INDEX idx_outbox_pending
    ON outbox_events(created_at) WHERE processed = FALSE;