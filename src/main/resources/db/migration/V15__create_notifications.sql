-- ============================================================================
-- notifications — notificações in-app (RF-35, RF-38, RF-39)
-- ============================================================================

CREATE TABLE notifications (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tipo         VARCHAR(30) NOT NULL
                 CHECK (tipo IN ('MATCH', 'MENSAGEM', 'AVALIACAO', 'DENUNCIA_RESOLVIDA', 'VERIFICACAO_APROVADA')),
    titulo       VARCHAR(150) NOT NULL,
    mensagem     TEXT,
    lida         BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- A tela de notificações sempre filtra "minhas notificações não lidas" —
-- índice composto cobre exatamente essa consulta.
CREATE INDEX idx_notifications_user_lida ON notifications (user_id, lida);
