-- ============================================================================
-- verificacao_locador — solicitações de verificação de identidade (RF-08/RF-09)
-- ============================================================================

CREATE TABLE verificacao_locador (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                            CHECK (status IN ('PENDENTE', 'APROVADO', 'REJEITADO')),
    documento_url           VARCHAR(500) NOT NULL,
    analisado_por           UUID REFERENCES users(id),
    justificativa_rejeicao  VARCHAR(500),
    data_criacao            TIMESTAMPTZ NOT NULL DEFAULT now(),
    data_analise            TIMESTAMPTZ
);

CREATE INDEX idx_verificacao_locador_user_id ON verificacao_locador (user_id);
CREATE INDEX idx_verificacao_locador_status  ON verificacao_locador (status);
