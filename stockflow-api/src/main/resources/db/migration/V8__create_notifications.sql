-- =========================================================================
-- V8__create_notifications.sql
-- Tabela de notificações de alertas de estoque
-- =========================================================================

CREATE TABLE notifications (
                               id                    UUID                  PRIMARY KEY DEFAULT uuid_generate_v4(),
                               version               BIGINT,
    -- ON DELETE CASCADE: se o produto for deletado, notificações são deletadas
                               product_id            UUID                  REFERENCES products(id) ON DELETE CASCADE,
                               product_name          VARCHAR(255)          NOT NULL,   -- snapshot do nome no momento
                               product_sku           VARCHAR(100)          NOT NULL,   -- snapshot do sku no momento
                               notification_type     notification_type     NOT NULL,
                               notification_priority notification_priority NOT NULL,
                               title                 VARCHAR(255)          NOT NULL,
                               message               TEXT                  NOT NULL,
                               current_quantity      INTEGER,
                               minimum_quantity      INTEGER,
                               is_read               BOOLEAN               NOT NULL DEFAULT FALSE,
                               is_resolved           BOOLEAN               NOT NULL DEFAULT FALSE,
    -- Usuário responsável por resolver a notificação
                               assigned_to           UUID                  REFERENCES users(id) ON DELETE SET NULL,
                               read_at               TIMESTAMPTZ,
                               resolved_at           TIMESTAMPTZ,
                               created_at            TIMESTAMPTZ           NOT NULL DEFAULT NOW(),
                               updated_at            TIMESTAMPTZ           NOT NULL DEFAULT NOW(),
                               created_by            UUID                  REFERENCES users(id) ON DELETE SET NULL,
                               updated_by            UUID                  REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- ÍNDICES
-- ==========================================
CREATE INDEX idx_notifications_product_id   ON notifications(product_id);
CREATE INDEX idx_notifications_type         ON notifications(notification_type);
CREATE INDEX idx_notifications_priority     ON notifications(notification_priority);
CREATE INDEX idx_notifications_assigned_to  ON notifications(assigned_to)
    WHERE assigned_to IS NOT NULL;
CREATE INDEX idx_notifications_created_at   ON notifications(created_at DESC);

-- Índices parciais para queries mais comuns (não lidas / não resolvidas)
CREATE INDEX idx_notifications_unread
    ON notifications(is_read) WHERE is_read = FALSE;

CREATE INDEX idx_notifications_unresolved
    ON notifications(is_resolved) WHERE is_resolved = FALSE;