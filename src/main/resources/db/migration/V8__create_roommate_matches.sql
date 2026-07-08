-- ============================================================================
-- roommate_matches — solicitações de match entre roommates (RF-34/RF-35)
-- ============================================================================

CREATE TABLE roommate_matches (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitante_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    destinatario_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                     CHECK (status IN ('PENDENTE', 'ACEITO', 'RECUSADO', 'CANCELADO')),
    mensagem_inicial TEXT,
    data_solicitacao TIMESTAMPTZ NOT NULL DEFAULT now(),
    data_resposta    TIMESTAMPTZ,

    CONSTRAINT chk_roommate_match_pessoas_distintas CHECK (solicitante_id <> destinatario_id),
    -- Regra de negócio (RF-34: "bloquear nova solicitação se já existe entre o
    -- par") garantida no próprio banco, não só na camada de serviço.
    CONSTRAINT uq_roommate_match_par UNIQUE (solicitante_id, destinatario_id)
);

CREATE INDEX idx_roommate_matches_destinatario ON roommate_matches (destinatario_id);
CREATE INDEX idx_roommate_matches_status       ON roommate_matches (status);

-- Nota para quem for implementar o serviço (T5.8): a UNIQUE acima só cobre o
-- par exato (A pede a B). Ela NÃO impede que B também peça a A depois — isso
-- ainda precisa ser checado na camada de serviço (buscar match nos dois
-- sentidos antes de criar um novo).
