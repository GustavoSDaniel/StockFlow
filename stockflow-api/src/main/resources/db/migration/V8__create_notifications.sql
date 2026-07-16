-- =========================================================================
-- V8__create_notifications.sql
-- Tabela de notificações de alertas de estoque
-- =========================================================================

CREATE TABLE notifications (
                               id                    UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                               version               BIGINT,
                               product_id            UUID          REFERENCES products(id) ON DELETE CASCADE,
                               product_name          VARCHAR(255)  NOT NULL,
                               product_sku           VARCHAR(100)  NOT NULL,
                               notification_type     VARCHAR(50)   NOT NULL,
                               notification_priority VARCHAR(50)   NOT NULL,
                               title                 VARCHAR(255)  NOT NULL,
                               message               TEXT          NOT NULL,
                               current_quantity      INTEGER,
                               minimum_quantity      INTEGER,
                               maximum_quantity      INTEGER,
                               reorder_point         INTEGER,
                               is_read               BOOLEAN       NOT NULL DEFAULT FALSE,
                               is_resolved           BOOLEAN       NOT NULL DEFAULT FALSE,
                               read_at               TIMESTAMPTZ,
                               resolved_at           TIMESTAMPTZ,
                               created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                               updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                               created_by            UUID,
                               updated_by            UUID
);

-- ==========================================
-- ÍNDICES
-- ==========================================
CREATE INDEX idx_notifications_product_id   ON notifications(product_id);
CREATE INDEX idx_notifications_type         ON notifications(notification_type);
CREATE INDEX idx_notifications_priority     ON notifications(notification_priority);

CREATE INDEX idx_notifications_created_at   ON notifications(created_at DESC);

-- Índices parciais para queries mais comuns (não lidas / não resolvidas)
CREATE INDEX idx_notifications_unread
    ON notifications(is_read) WHERE is_read = FALSE;

CREATE INDEX idx_notifications_unresolved
    ON notifications(is_resolved) WHERE is_resolved = FALSE;